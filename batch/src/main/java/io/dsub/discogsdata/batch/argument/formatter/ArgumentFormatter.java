package io.dsub.discogsdata.batch.argument.formatter;

/** Argument formatter interface to present formatter of specific argument value. */
public interface ArgumentFormatter {
  /**
   * Single method that performs formatting.
   *
   * @param arg argument to be evaluated.
   * @return result that is either being formatted, or ignored to be formatted.
   */
  String format(String arg);
}
