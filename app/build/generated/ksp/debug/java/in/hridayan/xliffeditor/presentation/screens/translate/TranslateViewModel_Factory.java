package in.hridayan.xliffeditor.presentation.screens.translate;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TranslateViewModel_Factory implements Factory<TranslateViewModel> {
  @Override
  public TranslateViewModel get() {
    return newInstance();
  }

  public static TranslateViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TranslateViewModel newInstance() {
    return new TranslateViewModel();
  }

  private static final class InstanceHolder {
    static final TranslateViewModel_Factory INSTANCE = new TranslateViewModel_Factory();
  }
}
