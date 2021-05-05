package io.dsub.discogsdata.common.entity.master;

import io.dsub.discogsdata.common.entity.Genre;
import io.dsub.discogsdata.common.entity.base.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "master_genre",
    uniqueConstraints = @UniqueConstraint(columnNames = {"master_id", "genre"}))
public class MasterGenre extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "master_id")
  @ManyToOne
  private Master master;

  @JoinColumn(name = "genre")
  @ManyToOne
  private Genre genre;
}
