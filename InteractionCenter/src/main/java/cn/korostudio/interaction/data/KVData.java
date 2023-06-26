package cn.korostudio.interaction.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table
public class KVData {
    @Id
    String KVKey;
    @Lob
    String KVData;

}
