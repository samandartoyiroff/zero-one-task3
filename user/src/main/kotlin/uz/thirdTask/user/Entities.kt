package uz.thirdTask.user

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import javax.persistence.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity(name = "users")
class User(
    @Column(unique = true, length = 32, nullable = false) var username: String,
    var password: String,
    @ManyToOne var role: Role,
    @Column(length = 128) var fullName: String,
    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    var status: UserStatus = UserStatus.PENDING,
) : BaseEntity()

@Entity(name = "roles")
class Role(
    @Column(unique = true, length = 64, nullable = false) var key: String,
    var name: String,
) : BaseEntity()
