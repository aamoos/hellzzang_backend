package com.hellzzang.service;

import com.hellzzang.dto.GymWearDto;
import com.hellzzang.dto.GymWearFileDto;
import com.hellzzang.dto.QGymWearDto;
import com.hellzzang.dto.QGymWearFileDto;
import com.hellzzang.entity.GymWear;
import com.hellzzang.repository.GymWearRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static com.hellzzang.entity.QGymWear.gymWear;
import static com.hellzzang.entity.QGymWearFile.gymWearFile;

/**
 * packageName    : com.hellzzang.service
 * fileName       : GymWearService
 * author         : 김재성
 * date           : 2023-06-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-12        김재성       최초 생성
 */

@RequiredArgsConstructor
@Service
@Transactional
public class GymWearService {

    private final GymWearRepository gymWearRepository;
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * @methodName : selectBannerList
     * @date : 2023-05-16 오후 1:52
     * @author : 김재성
     * @Description: 배너 페이징 조회
     **/
    public Page<GymWearDto> selectGymWearList(Pageable pageable, String searchVal){
        //admin 사용자 리스트 조회
        List<GymWearDto> content = getGymWearList(pageable, searchVal);

        //admin 사용자 total 조회
        Long count = getCount(searchVal);
        return new PageImpl<>(content, pageable, count);
    }

    /**
     * @methodName : getBannerList
     * @date : 2023-05-16 오후 1:51
     * @author : 김재성
     * @Description: 배너 리스트 조회
     **/
    private List<GymWearDto> getGymWearList(Pageable pageable, String searchVal) {

        List<GymWearDto> content = jpaQueryFactory
                .select(new QGymWearDto(
                        gymWear.id,
                        gymWear.title,
                        gymWear.contents,
                        gymWear.contentsText,
                        gymWear.adminUsers.userName,
                        gymWear.createdDate,
                        gymWear.lastModifiedDate,
                        gymWear.thumbnailIdx,
                        gymWear.delYn,
                        gymWear.price,
                        gymWear.optionYn
                ))
                .from(gymWear)
                .where(gymWear.delYn.eq("N"))
                .where(containsSearch(searchVal))
                .orderBy(gymWear.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return content;
//        return null;
    }

    /**
     * @methodName : getCount
     * @date : 2023-05-16 오후 1:51
     * @author : 김재성
     * @Description: 배너 total 조회
     **/
    private Long getCount(String searchVal){
        Long count = jpaQueryFactory
                .select(gymWear.count())
                .from(gymWear)
                .where(gymWear.delYn.eq("N"))
                .where(containsSearch(searchVal))
                .fetchOne();
        return count;
    }

    public GymWear find(Long id){
        return gymWearRepository.findById(id).get();
    }

    public List<GymWearFileDto> findGymWearFileList(Long id){
        List<GymWearFileDto> content = jpaQueryFactory
                .select(new QGymWearFileDto(
                        gymWearFile.fileInfo.id
                        ,gymWearFile.fileInfo
                ))
                .from(gymWearFile)
                .where(gymWearFile.fileInfo.delYn.eq("N"))
                .where(gymWearFile.gymWear.id.eq(id))
                .orderBy(gymWearFile.id.desc())
                .fetch();

        return content;
    }

    private BooleanExpression containsSearch(String searchVal){
        return searchVal != null && !searchVal.equals("") ? gymWear.title.contains(searchVal) : null;
    }

}
