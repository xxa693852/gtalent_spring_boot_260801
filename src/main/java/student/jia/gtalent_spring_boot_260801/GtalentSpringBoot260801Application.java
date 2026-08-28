package student.jia.gtalent_spring_boot_260801;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  //Spring Boot 專案的「啟動總開關」，標示著整個應用程式的入口點 1.設定與元件掃描的「藍圖/告示牌」
public class GtalentSpringBoot260801Application {

	public static void main(String[] args) {
		// 2. 真正執行啟動流程的「執行引擎」
		SpringApplication.run(GtalentSpringBoot260801Application.class, args);
	}

}