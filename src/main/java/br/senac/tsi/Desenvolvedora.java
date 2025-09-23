package br.senac.tsi.gamecatalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Desenvolvedora extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "O nome não pode ser vazio")
    @Size(min = 2, max = 100)
    public String nome;

    @Past(message = "A data de fundação deve ser no passado")
    public LocalDate fundacao;

    @NotBlank(message = "A nacionalidade é obrigatória")
    @Size(max = 80)
    public String nacionalidade;

    // Relacionamento Um-para-Um (lado dono)
    // Uma desenvolvedora tem um perfil. Cascade ALL significa que ao salvar/deletar
    // uma desenvolvedora, o perfil associado também será salvo/deletado.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "perfil_id")
    public br.senac.tsi.gamecatalog.PerfilDesenvolvedora perfil;

    // Relacionamento Um-para-Muitos
    // Uma desenvolvedora pode ter vários jogos.
    @OneToMany(mappedBy = "desenvolvedora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Evita loop na serialização
    public List<br.senac.tsi.gamecatalog.Jogo> jogos = new ArrayList<>();
}

