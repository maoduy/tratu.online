package online.tratu.login.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import online.tratu.model.LookupHistory;

@Repository("lookupHistoryRepository")
public interface LookupHistoryRepository extends JpaRepository<LookupHistory, Integer>{
	
}
