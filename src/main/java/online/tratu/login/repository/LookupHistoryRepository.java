package online.tratu.login.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import online.tratu.model.LookupHistory;

@Repository("lookupHistoryRepository")
public interface LookupHistoryRepository extends JpaRepository<LookupHistory, Integer> {
	@Query(value = "Select * from lookup_history t where user_user_id = :user_id order by point asc limit 30", nativeQuery = true)
	List<LookupHistory> findRandomHistory(@Param("user_id") String user_id);

	@Modifying
	@Transactional
	@Query(value = "Update lookup_history set point=point + :point where word=:word and user_user_id=:user_id", nativeQuery = true)
	void updatePoint(@Param("word") String word, @Param("point") int point, @Param("user_id") int user_id);

}
