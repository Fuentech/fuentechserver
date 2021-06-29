package co.com.fuentech.fuentechserver.security.entities;

import co.com.fuentech.fuentechserver.security.roles.Roles;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Roles name;
}
