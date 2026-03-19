package moment.like.infrastructure;

import java.util.Optional;
import moment.like.domain.MomentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MomentLikeRepository extends JpaRepository<MomentLike, Long> {

    Optional<MomentLike> findByMomentIdAndMemberId(Long momentId, Long memberId);

    @Query(value = "SELECT * FROM moment_likes WHERE moment_id = :momentId AND member_id = :memberId",
           nativeQuery = true)
    Optional<MomentLike> findByMomentIdAndMemberIdIncludeDeleted(
        @Param("momentId") Long momentId,
        @Param("memberId") Long memberId
    );

    long countByMomentId(Long momentId);

    boolean existsByMomentIdAndMemberId(Long momentId, Long memberId);
}
