package io.dsub.discogs.api.artist;

import io.dsub.discogs.api.exception.ArtistNotFoundException;
import io.dsub.discogs.common.entity.artist.Artist;
import io.dsub.discogs.common.repository.artist.ArtistAliasRepository;
import io.dsub.discogs.common.repository.artist.ArtistGroupRepository;
import io.dsub.discogs.common.repository.artist.ArtistMemberRepository;
import io.dsub.discogs.common.repository.artist.ArtistRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultArtistService implements ArtistService {

  private final ArtistRepository artistRepository;
  private final ArtistMemberRepository artistMemberRepository;
  private final ArtistGroupRepository artistGroupRepository;
  private final ArtistAliasRepository artistAliasRepository;

  @Override
  public ArtistDto getArtistById(long id) throws ArtistNotFoundException {
    Optional<Artist> optionalArtist = artistRepository.findById(id);
    if (optionalArtist.isEmpty()) {
      throw new ArtistNotFoundException("artist with id " + id + " not found");
    }
    return this.makeArtistDto(optionalArtist.get());
  }

  @Override
  public List<ArtistDto> getArtists() {
    return artistRepository.findAll(PageRequest.of(1, 50)).stream()
        .map(this::makeArtistDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<ArtistDto> getArtistsByName(String name) {
    return artistRepository.findAllByNameContains(name).stream()
        .map(this::makeArtistDto)
        .collect(Collectors.toList());
  }

  // todo: impl

  private ArtistDto makeArtistDto(Artist artist) {
    return null;
  }
}
