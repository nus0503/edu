package com.company.edu.service;

import com.company.edu.common.code.error.CommonErrorCode;
import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.code.error.WorksheetErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.worksheet.*;
import com.company.edu.dto.user.CustomUserInfoDto;
import com.company.edu.entity.problem.Problem;
import com.company.edu.entity.problem.Semesters;
import com.company.edu.entity.worksheet.Worksheet;
import com.company.edu.entity.user.Member;
import com.company.edu.entity.worksheet.WorksheetProblem;
import com.company.edu.repository.*;
import com.company.edu.repository.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WorksheetService {


    private final ProblemRepository problemRepository;

    private final MinorUnitRepository minorUnitRepository; // DetailedUnitRepository -> MinorUnitRepository

    private final SemesterRepository semesterRepository;

    private final WorksheetRepository worksheetRepository;
    private final MemberRepository memberRepository;
    private final WorksheetProblemRepository worksheetProblemRepository;

    public WorksheetResponse generateWorksheet(WorksheetRequest request) {
        // 1. 선택된 경로들에서 소단원명들 추출
//        List<String> unitNames = extractUnitNames(request.getSelectedPaths());

        List<Long> minorUnitIds = request.getMinorUnitIds();

        if (minorUnitIds == null || minorUnitIds.isEmpty()) {
            throw new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }


        // 2. levelWeight를 기반으로 난이도별 문제 개수 계산
        Map<String, Integer> difficultyTargets = calculateDifficultyTargets(
                request.getSettings().getProblemCount(),
                request.getSettings().getLevelWeight()
        );

        // 3. 문제 타입 필터
        String problemType = request.getSettings().getProblemType();


        // 4. 난이도별로 문제 조회 및 선택
        List<ProblemDTO> selectedProblems = selectProblemsByDifficulty(
                minorUnitIds, difficultyTargets, problemType
        );

        // 5. 통계 정보 생성
        WorksheetResponse.WorksheetStatistics statistics = generateStatistics(minorUnitIds, selectedProblems);

        // 6. 응답 생성
        WorksheetResponse response = new WorksheetResponse();
        response.setProblems(selectedProblems);
        response.setStatistics(statistics);

        return response;
    }


    public WorksheetResponse.AddNewProblemsResponseDto addNewProblems(WorksheetRequest.AddNewProblemsRequestDto request) {
        // 1. 선택된 경로들에서 소단원명들 추출
//        List<String> unitNames = extractUnitNames(request.getSelectedPaths());

        List<Long> minorUnitIds = request.getMinorUnitIds();

        if (minorUnitIds == null || minorUnitIds.isEmpty()) {
            throw new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }


        // 2. 제외할 문제 ID 목록
        Set<Long> excludeIds = request.getExcludeProblemIds() != null ? new HashSet<Long>(request.getExcludeProblemIds()) : new HashSet<Long>();

        // 3. 문제 타입 필터
        String problemType = request.getSettings().getProblemType();

        // 4. 페이징 정보 설정
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 5. 전체 이용 가능한 문제 수 조회 (제외 문제 제외)
        int totalAvailableProblems = problemRepository.countAvailableProblemsExcluding(minorUnitIds, problemType, excludeIds);


        Map<String, Integer> stringIntegerMap = calculateDifficultyTargets(request.getSize(), request.getSettings().getLevelWeight());


        List<ProblemDTO> availableProblems  = problemRepository.findProblemsByUnitsAndFiltersExcludingWithPaging(minorUnitIds, null, problemType, excludeIds, pageable);

        // 7. 요청된 문제 수만큼 선택 (페이징된 결과에서)
        int selectCount = Math.min(request.getProblemCount(), availableProblems.size());

        List<ProblemDTO> selectedProblems = new ArrayList<>();

        Collections.shuffle(availableProblems); // 랜덤 섞기
        selectedProblems = availableProblems.subList(0, selectCount);

        // 8. 페이징 정보 생성
        int totalPages = (int) Math.ceil((double) totalAvailableProblems / request.getSize());
        WorksheetResponse.AddNewProblemsResponseDto.PageInfo pageInfo =
                new WorksheetResponse.AddNewProblemsResponseDto.PageInfo();
        pageInfo.setCurrentPage(request.getPage());
        pageInfo.setPageSize(request.getSize());
        pageInfo.setTotalElements(totalAvailableProblems);
        pageInfo.setTotalPages(totalPages);
        pageInfo.setHasNext(request.getPage() < totalPages - 1);
        pageInfo.setHasPrevious(request.getPage() > 0);
        pageInfo.setFirst(request.getPage() == 0);
        pageInfo.setLast(request.getPage() == totalPages - 1);

        // 10. 응답 생성
        WorksheetResponse.AddNewProblemsResponseDto response = new WorksheetResponse.AddNewProblemsResponseDto();
        response.setProblems(selectedProblems);
        response.setPageInfo(pageInfo);

        return response;
    }





    /**
     * levelWeight 배열을 기반으로 난이도별 목표 문제 개수 계산
     * @param totalCount 총 문제 개수
     * @param levelWeight [하, 중하, 중, 상, 최상] 순서의 가중치 배열
     * @return 난이도별 목표 개수 맵
     */
    private Map<String, Integer> calculateDifficultyTargets(int totalCount, List<Integer> levelWeight) {
        Map<String, Integer> targets = new LinkedHashMap<>();

        if (levelWeight == null || levelWeight.size() != 5) {
            // 기본값: 중 난이도 기준
            levelWeight = Arrays.asList(10, 30, 30, 25, 5);
        }

        String[] difficulties = {"하", "중하", "중", "상", "최상"};
        int assignedTotal = 0;

        // 처음 4개 난이도는 비율 계산
        for (int i = 0; i < 4; i++) {
            int count = Math.round((float) totalCount * levelWeight.get(i) / 100);
            targets.put(difficulties[i], count);
            assignedTotal += count;
        }

        // 마지막 난이도는 남은 개수 할당 (반올림 오차 보정)
        targets.put(difficulties[4], totalCount - assignedTotal);

        return targets;
    }

    /**
     * 난이도별 목표에 맞춰 문제 선택
     */
    private List<ProblemDTO> selectProblemsByDifficulty(
            List<Long> minorUnitIds,
            Map<String, Integer> difficultyTargets,
            String problemType
    ) {
        List<ProblemDTO> selectedProblems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : difficultyTargets.entrySet()) {
            String difficulty = entry.getKey();
            int targetCount = entry.getValue();

            if (targetCount <= 0) continue;

            // 해당 난이도의 문제들 조회
            List<ProblemDTO> availableProblems = problemRepository.findProblemsByUnitIdsAndFilters(
                    minorUnitIds,
                    Collections.singletonList(difficulty),
                    problemType
            );

            // 목표 개수만큼 선택 (부족하면 있는 만큼만)
            int selectCount = Math.min(targetCount, availableProblems.size());

            if (selectCount > 0) {
                // 랜덤하게 선택하거나 순서대로 선택
                Collections.shuffle(availableProblems); // 랜덤 선택을 위한 셔플
                selectedProblems.addAll(availableProblems.subList(0, selectCount));
            }

            // 부족한 경우 로그 출력 (선택사항)
            if (selectCount < targetCount) {
                System.out.println("난이도 '" + difficulty + "' 문제 부족: 요청=" + targetCount + ", 실제=" + selectCount);
            }
        }

        // 문제가 부족한 경우 다른 난이도에서 보충
        int totalSelected = selectedProblems.size();
        int totalTarget = difficultyTargets.values().stream().mapToInt(Integer::intValue).sum();

        if (totalSelected < totalTarget) {
            selectedProblems.addAll(fillShortage(minorUnitIds, problemType, totalTarget - totalSelected, selectedProblems));
        }

        return selectedProblems;
    }

    /**
     * 부족한 문제를 다른 난이도에서 보충
     */
    private List<ProblemDTO> fillShortage(
            List<Long> minorUnitIds,
            String problemType,
            int shortage,
            List<ProblemDTO> alreadySelected
    ) {
        List<ProblemDTO> additionalProblems = new ArrayList<>();

        // 이미 선택된 문제 ID 목록
        Set<Long> selectedIds = alreadySelected.stream()
                .map(ProblemDTO::getId)
                .collect(Collectors.toSet());

        // 모든 문제 조회 (난이도 제한 없음)
        List<ProblemDTO> allAvailable = problemRepository.findProblemsByUnitIdsAndFilters(
                minorUnitIds, null, problemType
        );

        // 이미 선택된 문제 제외
        List<ProblemDTO> candidates = allAvailable.stream()
                .filter(p -> !selectedIds.contains(p.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(candidates);

        int fillCount = Math.min(shortage, candidates.size());
        if (fillCount > 0) {
            additionalProblems.addAll(candidates.subList(0, fillCount));
        }

        return additionalProblems;
    }

    private List<String> extractUnitNames(List<String> selectedPaths) {
        List<String> unitNames = new ArrayList<>();

        for (String path : selectedPaths) {
            String[] parts = path.split(" > ");

            if (parts.length >= 1) {
                // 학기 노드인 경우
                if (parts.length == 1) {
                    String semesterInfo = parts[0];
                    unitNames.addAll(getUnitNamesBySemester(semesterInfo));
                }
                // 대단원인 경우
                else if (parts.length == 2) {
                    String majorUnitName = parts[1];
                    unitNames.addAll(getUnitNamesByMajorUnit(majorUnitName));
                }
                // 중단원인 경우
                else if (parts.length == 3) {
                    String middleUnitName = parts[2];
                    unitNames.addAll(getUnitNamesByMiddleUnit(middleUnitName));
                }
                // 소단원인 경우 (최하위 레벨)
                else if (parts.length == 4) {
                    String minorUnitName = parts[3];
                    unitNames.add(minorUnitName);
                }
            }
        }

        return unitNames.stream().distinct().collect(Collectors.toList());
    }

    private List<String> getUnitNamesBySemester(String semesterInfo) {
        String[] parts = semesterInfo.split(" ");
        if (parts.length != 2) return new ArrayList<>();

        String gradeStr = parts[0];
        String semesterName = parts[1];

        Semesters.GradeLevel gradeLevel = parseGradeLevel(gradeStr);

        return minorUnitRepository.findBySemester(gradeLevel, semesterName)
                .stream()
                .map(mu -> mu.getName())
                .collect(Collectors.toList());
    }

    private List<String> getUnitNamesByMajorUnit(String majorUnitName) {
        return minorUnitRepository.findByMajorUnitName(majorUnitName)
                .stream()
                .map(mu -> mu.getName())
                .collect(Collectors.toList());
    }

    private List<String> getUnitNamesByMiddleUnit(String middleUnitName) {
        List<String> collect = minorUnitRepository.findByMiddleUnitName(middleUnitName)
                .stream()
                .map(mu -> mu.getName())
                .collect(Collectors.toList());
        return collect;
    }

    private Semesters.GradeLevel parseGradeLevel(String gradeStr) {
        switch (gradeStr) {
            case "초": return Semesters.GradeLevel.elementary;
            case "중": return Semesters.GradeLevel.middle;
            case "고": return Semesters.GradeLevel.high;
            default: return Semesters.GradeLevel.elementary;
        }
    }

    private WorksheetResponse.WorksheetStatistics generateStatistics(List<Long> minorUnitIds, List<ProblemDTO> problems) {
        WorksheetResponse.WorksheetStatistics statistics = new WorksheetResponse.WorksheetStatistics();

        // 기본 통계
        statistics.setTotalProblems(problems.size());
        statistics.setMultipleChoice((int) problems.stream().filter(p -> "객관식".equals(p.getProblemType())).count());
        statistics.setSubjective((int) problems.stream().filter(p -> "주관식".equals(p.getProblemType())).count());
        statistics.setShortAnswer((int) problems.stream().filter(p -> "서술형".equals(p.getProblemType())).count());

        // 실제 ProblemStats에서 평균 정답률 계산
        Double avgCorrectRate = problemRepository.getAverageCorrectRateByUnits(minorUnitIds);
        statistics.setNationalAverageCorrectRate(avgCorrectRate != null ? avgCorrectRate : 75.0);

        // 실제 선택된 문제들의 난이도별 분포 (실제 개수)
        WorksheetResponse.DifficultyDistribution distribution = new WorksheetResponse.DifficultyDistribution();
        distribution.setLow((int) problems.stream().filter(p -> "하".equals(p.getDifficulty())).count());
        distribution.setMediumLow((int) problems.stream().filter(p -> "중하".equals(p.getDifficulty())).count());
        distribution.setMedium((int) problems.stream().filter(p -> "중".equals(p.getDifficulty())).count());
        distribution.setHigh((int) problems.stream().filter(p -> "상".equals(p.getDifficulty())).count());
        distribution.setVeryHigh((int) problems.stream().filter(p -> "최상".equals(p.getDifficulty())).count());

        statistics.setDifficultyDistribution(distribution);

        return statistics;
    }
    @Transactional
    public void saveWorksheet(WorksheetRequest.WorksheetCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CustomUserInfoDto member = userDetails.getMember();
        Member findMember = memberRepository.findById(member.getMemberId()).orElseThrow(
                () -> new RestApiException(UserErrorCode.NOT_FOUND_USER)
        );
        if (!findMember.getMemberId().equals(request.getAuthorId())) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }
        Worksheet worksheet = Worksheet.generateEntity(request, findMember);
        Worksheet savedWorksheet = worksheetRepository.save(worksheet);

        List<WorksheetProblem> worksheetProblems = new ArrayList<>();
        for (WorksheetRequest.WorksheetCreateRequest.ProblemOrder problemOrder : request.getProblemOrders()) {
            Problem problem = problemRepository.findById(problemOrder.getProblemId()).orElseThrow(() -> new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND));

            WorksheetProblem build = WorksheetProblem.builder()
                    .worksheet(savedWorksheet)
                    .problem(problem)
                    .problemOrder(problemOrder.getOrder())
                    .build();

            worksheetProblems.add(build);
        }
        worksheetProblemRepository.saveAll(worksheetProblems);
    }

    public WorksheetResponse.WorksheetListResponse getWorksheet(String page, String size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CustomUserInfoDto member = userDetails.getMember();
        Member memberEntity = memberRepository.findById(member.getMemberId()).orElseThrow(
                () -> new RestApiException(UserErrorCode.NOT_FOUND_USER)
        );

        int pageNumber = Integer.parseInt(page);
        int pageSize = Integer.parseInt(size);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Worksheet> worksheetPage = worksheetRepository.findByStatusAndAuthorId(Worksheet.Status.ACTIVE, memberEntity, pageable);

        // DTO 변환
        List<WorksheetResponse.WorksheetListResponse.WorksheetData.WorksheetInfo> content =
                worksheetPage.getContent().stream()
                        .map(worksheet -> WorksheetResponse.WorksheetListResponse.WorksheetData.WorksheetInfo.builder()
                                .worksheetId(worksheet.getWorksheetId())
                                .authorId(worksheet.getAuthorId().getMemberId())
                                .tester(worksheet.getTester())
                                .grade(worksheet.getGrade())
                                .tag(worksheet.getTag())
                                .title(worksheet.getTitle())
                                .description(worksheet.getDescription())
                                .problemCount(worksheet.getProblemCount())
                                .contentRange(worksheet.getContentRange())
                                .createdAt(worksheet.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

        // 응답 구성
        WorksheetResponse.WorksheetListResponse.WorksheetData data =
                WorksheetResponse.WorksheetListResponse.WorksheetData.builder()
                        .content(content)
                        .empty(worksheetPage.isEmpty())
                        .first(worksheetPage.isFirst())
                        .last(worksheetPage.isLast())
                        .number(worksheetPage.getNumber())
                        .numberOfElements(worksheetPage.getNumberOfElements())
                        .size(worksheetPage.getSize())
                        .totalElements((int) worksheetPage.getTotalElements())
                        .totalPages(worksheetPage.getTotalPages())
                        .build();

        return WorksheetResponse.WorksheetListResponse.builder()
                .data(data)
                .build();
    }

    /**
     * 학습지 조회
     */
    @Transactional(readOnly = true)
    public Worksheet getWorksheetById(Long worksheetId) {
        return worksheetRepository.findById(worksheetId)
                .orElseThrow(() -> new RestApiException(WorksheetErrorCode.WORKSHEET_NOT_FOUND));
    }



    /**
     * 학습지의 문제들을 순서대로 조회
     */
    @Transactional
    public List<WorksheetProblem> getWorksheetProblemsOrdered(Long worksheetId) {
        Worksheet worksheet = getWorksheetById(worksheetId);
        List<WorksheetProblem> byWorksheetOrderByProblemOrderAsc = worksheetProblemRepository.findByWorksheetOrderByProblemOrderAsc(worksheet);
        for (WorksheetProblem worksheetProblem : byWorksheetOrderByProblemOrderAsc) {
            worksheetProblem.getProblem().getId();
        }
        return byWorksheetOrderByProblemOrderAsc;
    }

    @Transactional
    public SavedWorksheetResponseDto getSavedWorksheetProblem(Long worksheetId) {
        Worksheet worksheet = worksheetRepository.findById(worksheetId).orElseThrow(
                () -> new RestApiException(WorksheetErrorCode.WORKSHEET_NOT_FOUND)
        );
        List<WorksheetProblem> allById = worksheetProblemRepository.findAllByWorksheetOrderByProblemOrderAsc(worksheet);

        List<ProblemDTO> problems = new ArrayList<>();
        allById.stream().map(WorksheetProblem::getProblem).forEach(problem -> {
            int i = 1;
            log.debug("i = {}", i);
            String name = problem.getMinorUnit().getName();
            BigDecimal correctRate = problem.getProblemStats().getCorrectRate();
            problems.add(new ProblemDTO(problem));
        });

        SavedWorksheetResponseDto.WorksheetStatistics statistics = new SavedWorksheetResponseDto.WorksheetStatistics();

        // 기본 통계
        statistics.setTotalProblems(problems.size());
        statistics.setMultipleChoice((int) problems.stream().filter(p -> "객관식".equals(p.getProblemType())).count());
        statistics.setSubjective((int) problems.stream().filter(p -> "주관식".equals(p.getProblemType())).count());
        statistics.setShortAnswer((int) problems.stream().filter(p -> "서술형".equals(p.getProblemType())).count());

        // 실제 ProblemStats에서 평균 정답률 계산
        Double avgCorrectRate = null;
//        avgCorrectRate = problemRepository.getAverageCorrectRateByUnits(unitNames);
        statistics.setNationalAverageCorrectRate(avgCorrectRate != null ? avgCorrectRate : 75.0);

        // 실제 선택된 문제들의 난이도별 분포 (실제 개수)
        SavedWorksheetResponseDto.DifficultyDistribution distribution = new SavedWorksheetResponseDto.DifficultyDistribution();
        distribution.setLow((int) problems.stream().filter(p -> "하".equals(p.getDifficulty())).count());
        distribution.setMediumLow((int) problems.stream().filter(p -> "중하".equals(p.getDifficulty())).count());
        distribution.setMedium((int) problems.stream().filter(p -> "중".equals(p.getDifficulty())).count());
        distribution.setHigh((int) problems.stream().filter(p -> "상".equals(p.getDifficulty())).count());
        distribution.setVeryHigh((int) problems.stream().filter(p -> "최상".equals(p.getDifficulty())).count());

        statistics.setDifficultyDistribution(distribution);

        WorksheetSettingResponseDto setting = new WorksheetSettingResponseDto(worksheet.getProblemCount(), worksheet.getDifficulty(), worksheet.getProblemType(), worksheet.getAutoGrading(), worksheet.getMockExamIncluded());

        SavedWorksheetResponseDto worksheetResponse = new SavedWorksheetResponseDto();
        worksheetResponse.setProblems(problems);
        worksheetResponse.setStatistics(statistics);
        worksheetResponse.setSetting(setting);
        worksheetResponse.setAuthorId(worksheet.getAuthorId().getMemberId());
        worksheetResponse.setTester(worksheet.getTester());
        worksheetResponse.setTag(worksheet.getTag());
        worksheetResponse.setTitle(worksheet.getTitle());

        return worksheetResponse;
//        problemRepository.findSavedWorksheetProblem()
    }
    @Transactional
    public void updateWorksheet(Long worksheetId, UpdateWorksheetRequestDto request) {

        // 1. 현재 사용자 정보 확인 (권한 체크)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getMember().getMemberId();

        // 2. 학습지 조회 및 작성자 확인
        Worksheet worksheet = worksheetRepository.findById(worksheetId)
                .orElseThrow(() -> new RestApiException(WorksheetErrorCode.WORKSHEET_NOT_FOUND));

        if (!worksheet.getAuthorId().getMemberId().equals(currentUserId)) {
            throw new RestApiException(WorksheetErrorCode.WORKSHEET_ACCESS_DENIED);
        }

        // 3. 학습지 기본 정보 업데이트 (예: 문제 수)
        worksheet.updateProblemCountAndDescription(request.getProblemCount(), request.getTitle(), request.getTester(), request.getTag());

        // 4. 기존 문제 순서 정보 모두 삭제
        worksheetProblemRepository.deleteByWorksheetId(worksheet.getWorksheetId());

        // 5. 새로운 문제 순서 정보 생성 및 저장
        List<WorksheetProblem> newWorksheetProblems = new ArrayList<>();
        for (UpdateWorksheetRequestDto.ProblemOrder problemOrder : request.getProblemOrders()) {
            Problem problem = problemRepository.findById(problemOrder.getProblemId())
                    .orElseThrow(() -> new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND));

            WorksheetProblem newWp = WorksheetProblem.builder()
                    .worksheet(worksheet)
                    .problem(problem)
                    .problemOrder(problemOrder.getOrder())
                    .build();
            newWorksheetProblems.add(newWp);
        }
        worksheetProblemRepository.saveAll(newWorksheetProblems);

        log.info("✅ 학습지 업데이트 완료: worksheetId={}", worksheetId);
    }

    @Transactional
    public BulkDeleteResponseDto deleteWorksheet(List<Long> worksheetId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RestApiException(UserErrorCode.NOT_FOUND_USER));
        List<Long> successList = new ArrayList<>();
        List<Long> failedList = new ArrayList<>(worksheetId);
        BulkDeleteResponseDto bulkDeleteResponseDto = new BulkDeleteResponseDto();

        for (Long id : worksheetId) {
            Worksheet worksheet = worksheetRepository.findById(id).orElseThrow(() -> new RestApiException(WorksheetErrorCode.WORKSHEET_NOT_FOUND));
            if (!worksheet.getAuthorId().getMemberId().equals(member.getMemberId())) {
                throw new RestApiException(WorksheetErrorCode.WORKSHEET_ACCESS_DENIED);
            }
            worksheetRepository.delete(worksheet);
            successList.add(id);
            failedList.remove(id);
            bulkDeleteResponseDto.setSuccessIds(successList);
            bulkDeleteResponseDto.setFailedIds(failedList);
        }
        return bulkDeleteResponseDto;


    }
}