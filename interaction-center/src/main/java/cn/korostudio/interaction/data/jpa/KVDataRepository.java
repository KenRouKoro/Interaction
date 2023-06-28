package cn.korostudio.interaction.data.jpa;

import cn.korostudio.interaction.data.KVData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KVDataRepository extends JpaRepository<KVData,String> {
    KVData findByKVKey(String key);
    void deleteByKVKey(String key);

}
