package com.company.edu.config;//package com.company.education.config;
//
//import com.company.education.util.KatexHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.thymeleaf.dialect.IDialect;
//import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
//import org.thymeleaf.spring5.SpringTemplateEngine;
//import org.thymeleaf.templateresolver.ITemplateResolver;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Configuration
//public class ThymeleafConfig {
//
//    @Bean
//    public KatexHelper katexHelper() {
//        return new KatexHelper();
//    }
//
//    @Bean
//    public SpringTemplateEngine templateEngine(
//            @Autowired ITemplateResolver templateResolver,
//            @Autowired KatexHelper katexHelper
//    ) {
//        SpringTemplateEngine engine = new SpringTemplateEngine();
//        engine.setTemplateResolver(templateResolver);
//
//        Set<IDialect> dialects = new HashSet<>();
//        dialects.add(new Java8TimeDialect());
//        engine.setAdditionalDialects(dialects);
//
//        // 헬퍼 등록
//        engine.setAdditionalStaticVariables(
//                Collections.singletonMap("katex", katexHelper)
//        );
//
//        return engine;
//    }
//}