package io.dsub.discogs.common.repository.artist;

import io.dsub.discogs.common.entity.artist.Artist;
import io.dsub.discogs.common.entity.artist.ArtistGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistGroupRepository extends JpaRepository<ArtistGroup, Long> {

  boolean existsByArtistAndGroup(Artist artist, Artist group);

  List<ArtistGroup> findAllByArtistId(Long artistId);
}