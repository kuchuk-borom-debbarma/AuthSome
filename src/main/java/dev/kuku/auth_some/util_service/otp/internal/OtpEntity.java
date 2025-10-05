package dev.kuku.auth_some.util_service.otp.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity(name = "otps") //name of the table
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @Column(name = "otp", nullable = false)
    public String otp;
    @JdbcTypeCode(SqlTypes.JSON)
    //columnDefinition = the type of the data, name = the column name to be saved as in the database
    @Column(columnDefinition = "jsondb", name = "custom_data")
    public Map<String, Object> customData = new HashMap<>();
    @Column(name = "created_at")
    public long createdAt;
    @Column(name = "updated_at")
    public long updatedAt;
}
