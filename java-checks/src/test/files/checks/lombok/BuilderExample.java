import lombok.Builder;
import lombok.Singular;
import java.util.Set;

@Builder
@Deprecated
public class BuilderExample {
  @Builder
  private String name;
  private int age;
  @Singular
  private Set<String> occupations;
}
