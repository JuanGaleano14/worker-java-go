package com.global.worker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Deshabilitado para evitar fallos por dependencias externas en pruebas unitarias")
class WorkerApplicationTests {

	@Test
	void contextLoads() {
	}

}
