package student.jia.gtalent_spring_boot_260801.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
  public class LineBindRequest {

      @NotBlank
      private String idToken;
  }
