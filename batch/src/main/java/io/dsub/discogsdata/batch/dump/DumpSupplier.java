package io.dsub.discogsdata.batch.dump;

import java.util.List;
import java.util.function.Supplier;

public interface DumpSupplier extends Supplier<List<DiscogsDump>> {
  @Override
  List<DiscogsDump> get();
}