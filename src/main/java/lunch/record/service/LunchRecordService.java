package lunch.record.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lunch.record.domain.LunchRecord;
import lunch.record.domain.LunchRecordGroup;
import lunch.record.repository.LunchRecordRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LunchRecordService {

    private final LunchRecordRepository lunchRecordRepository;

    public Float getAverageGrade(LunchRecord lunchRecord) throws SQLException {
        float averageGrade;
        // 평점 획득
        Float grade = lunchRecord.getGrade();
        // 식당의 메뉴 기록 조회
        List<LunchRecord> byRestaurantMenu = lunchRecordRepository.findByRestaurantMenu(lunchRecord.getRestaurant(), lunchRecord.getMenu());
        validation(byRestaurantMenu);
        if (byRestaurantMenu.isEmpty()) {
            averageGrade = grade;
        } else {
            // 평점 적용
            if (lunchRecord.getId() == null) {
                // 식당의 메뉴 기록 추가
                byRestaurantMenu.add(lunchRecord);
            } else {
                byRestaurantMenu.stream()
                        .filter(it -> it.getId().equals(lunchRecord.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException())
                        .setGrade(grade);
            }

            // 식당의 같은 메뉴에 대한 기록 평점의 평균을 반환
            averageGrade = (float) byRestaurantMenu.stream()
                    .mapToDouble(LunchRecord::getGrade)
                    .average()
                    .getAsDouble();
        }

        return (float) (Math.round(averageGrade * 1000) / 1000.0);
    }

    public void correctAverageGrade() throws SQLException {
        List<LunchRecord> all = lunchRecordRepository.findAll();

        // '식당', '메뉴' 2개의 인수로 그룹화
//        Map<String, Map<String, List<LunchRecord>>> restaurantMap = all.stream()
//                .collect(Collectors.groupingBy(LunchRecord::getRestaurant, Collectors.groupingBy(LunchRecord::getMenu)));
//
//        for (String restaurant : restaurantMap.keySet()) {
//            Map<String, List<LunchRecord>> menuMap = restaurantMap.get(restaurant);
//            for (String menu : menuMap.keySet()) {
//                List<LunchRecord> lunchRecords = menuMap.get(menu);
//                float averageGrade = lunchRecords.stream()
//                        .collect(Collectors.averagingDouble(LunchRecord::getGrade))
//                        .floatValue();
//                lunchRecordRepository.updateAverageGradeByRestaurantMenu(averageGrade, restaurant, menu);
//            }
//        }

        // LunchRecordGroup 클래스로 그룹화
        Map<LunchRecordGroup, Double> collect = all.stream()
                .collect(Collectors.groupingBy(LunchRecordGroup::new, Collectors.averagingDouble(LunchRecord::getGrade)));

        for (LunchRecordGroup lunchRecordGroup : collect.keySet()) {
            float averageGrade = (float) (Math.round(collect.get(lunchRecordGroup) * 1000) / 1000.0);
            log.info("restaurant={}, menu={}, averageGrade={}", lunchRecordGroup.restaurant, lunchRecordGroup.menu, averageGrade);
            lunchRecordRepository.updateAverageGradeByRestaurantMenu(averageGrade, lunchRecordGroup.restaurant, lunchRecordGroup.menu);
        }
    }

    // 예외 상황 테스트를 위한 밸리데이션
    private void validation(List<LunchRecord> lunchRecords) {
        for (LunchRecord lunchRecord : lunchRecords) {
            if (lunchRecord.getGrade().equals(0.0f)) {
                throw new IllegalStateException("평점 평균 적용 중 예외 발생");
            }
        }
    }
}
