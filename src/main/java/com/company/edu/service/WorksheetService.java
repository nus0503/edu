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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        // 2. 필터 조건 설정
        List<String> difficulties = request.getSettings().getDifficulties();
        String problemType = request.getSettings().getProblemType();
        int problemCount = request.getSettings().getProblemCount();

        // 3. 문제 조회
        List<ProblemDTO> allProblems = problemRepository.findProblemsByUnitsAndFilters(
                unitNames, difficulties, problemType
        );

        // 4. 문제 수 제한 적용
        List<ProblemDTO> selectedProblems = selectProblems(allProblems, problemCount);

        // 5. 통계 정보 생성
        WorksheetResponse.WorksheetStatistics statistics = generateStatistics(unitNames, selectedProblems);

        // 6. 응답 생성
        WorksheetResponse response = new WorksheetResponse();
        response.setProblems(selectedProblems);
        response.setStatistics(statistics);

        return response;
    }

    private List<String> extractUnitNames(List<String> selectedPaths) {
        List<String> unitNames = new ArrayList<>();

        for (String path : selectedPaths) {
            String[] parts = path.split(" > ");

            if (parts.length >= 2) {
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

    private List<ProblemDTO> selectProblems(List<ProblemDTO> allProblems, int maxCount) {
        if (allProblems.size() <= maxCount) {
            return allProblems;
        }

        // 난이도별 균등 분배 로직
        Map<String, List<ProblemDTO>> problemsByDifficulty = allProblems.stream()
                .collect(Collectors.groupingBy(ProblemDTO::getDifficulty));

        List<ProblemDTO> selectedProblems = new ArrayList<>();
        int remainingCount = maxCount;

        for (List<ProblemDTO> problems : problemsByDifficulty.values()) {
            int countToSelect = Math.min(problems.size(), remainingCount / problemsByDifficulty.size());
            selectedProblems.addAll(problems.subList(0, countToSelect));
            remainingCount -= countToSelect;
        }

        return selectedProblems;
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

        // 난이도별 분포
        WorksheetResponse.DifficultyDistribution distribution = new WorksheetResponse.DifficultyDistribution();
        distribution.setLow(problemRepository.countProblemsByUnitsAndDifficulty(unitNames, "하"));
        distribution.setMediumLow(problemRepository.countProblemsByUnitsAndDifficulty(unitNames, "중하"));
        distribution.setMedium(problemRepository.countProblemsByUnitsAndDifficulty(unitNames, "중"));
        distribution.setHigh(problemRepository.countProblemsByUnitsAndDifficulty(unitNames, "상"));
        distribution.setVeryHigh(problemRepository.countProblemsByUnitsAndDifficulty(unitNames, "최상"));

        statistics.setDifficultyDistribution(distribution);

        return statistics;
    }
}