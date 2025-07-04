package com.company.edu.service;

import com.company.edu.dto.ProblemDTO;
import com.company.edu.dto.WorksheetRequest;
import com.company.edu.dto.WorksheetResponse;
import com.company.edu.entity.Semesters;
import com.company.edu.repository.MinorUnitRepository;
import com.company.edu.repository.ProblemRepository;
import com.company.edu.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorksheetService {


    private final ProblemRepository problemRepository;

    private final MinorUnitRepository minorUnitRepository; // DetailedUnitRepository -> MinorUnitRepository

    private final SemesterRepository semesterRepository;

    public WorksheetResponse generateWorksheet(WorksheetRequest request) {
        // 1. 선택된 경로들에서 소단원명들 추출
        List<String> unitNames = extractUnitNames(request.getSelectedPaths());

        // 2. levelWeight를 기반으로 난이도별 문제 개수 계산
        Map<String, Integer> difficultyTargets = calculateDifficultyTargets(
                request.getSettings().getProblemCount(),
                request.getSettings().getLevelWeight()
        );

        // 3. 문제 타입 필터
        String problemType = request.getSettings().getProblemType();

        // 4. 난이도별로 문제 조회 및 선택
        List<ProblemDTO> selectedProblems = selectProblemsByDifficulty(
                unitNames, difficultyTargets, problemType
        );

        // 5. 통계 정보 생성
        WorksheetResponse.WorksheetStatistics statistics = generateStatistics(unitNames, selectedProblems);

        // 6. 응답 생성
        WorksheetResponse response = new WorksheetResponse();
        response.setProblems(selectedProblems);
        response.setStatistics(statistics);

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
            List<String> unitNames,
            Map<String, Integer> difficultyTargets,
            String problemType
    ) {
        List<ProblemDTO> selectedProblems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : difficultyTargets.entrySet()) {
            String difficulty = entry.getKey();
            int targetCount = entry.getValue();

            if (targetCount <= 0) continue;

            // 해당 난이도의 문제들 조회
            List<ProblemDTO> availableProblems = problemRepository.findProblemsByUnitsAndFilters(
                    unitNames,
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
            selectedProblems.addAll(fillShortage(unitNames, problemType, totalTarget - totalSelected, selectedProblems));
        }

        return selectedProblems;
    }

    /**
     * 부족한 문제를 다른 난이도에서 보충
     */
    private List<ProblemDTO> fillShortage(
            List<String> unitNames,
            String problemType,
            int shortage,
            List<ProblemDTO> alreadySelected
    ) {
        List<ProblemDTO> additionalProblems = new ArrayList<>();

        // 이미 선택된 문제 ID 목록
        Set<Integer> selectedIds = alreadySelected.stream()
                .map(ProblemDTO::getId)
                .collect(Collectors.toSet());

        // 모든 문제 조회 (난이도 제한 없음)
        List<ProblemDTO> allAvailable = problemRepository.findProblemsByUnitsAndFilters(
                unitNames, null, problemType
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
        return minorUnitRepository.findByMiddleUnitName(middleUnitName)
                .stream()
                .map(mu -> mu.getName())
                .collect(Collectors.toList());
    }

    private Semesters.GradeLevel parseGradeLevel(String gradeStr) {
        switch (gradeStr) {
            case "초": return Semesters.GradeLevel.elementary;
            case "중": return Semesters.GradeLevel.middle;
            case "고": return Semesters.GradeLevel.high;
            default: return Semesters.GradeLevel.elementary;
        }
    }

    private WorksheetResponse.WorksheetStatistics generateStatistics(List<String> unitNames, List<ProblemDTO> problems) {
        WorksheetResponse.WorksheetStatistics statistics = new WorksheetResponse.WorksheetStatistics();

        // 기본 통계
        statistics.setTotalProblems(problems.size());
        statistics.setMultipleChoice((int) problems.stream().filter(p -> "객관식".equals(p.getProblemType())).count());
        statistics.setSubjective((int) problems.stream().filter(p -> "주관식".equals(p.getProblemType())).count());
        statistics.setShortAnswer((int) problems.stream().filter(p -> "서술형".equals(p.getProblemType())).count());

        // 실제 ProblemStats에서 평균 정답률 계산
        Double avgCorrectRate = problemRepository.getAverageCorrectRateByUnits(unitNames);
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
}