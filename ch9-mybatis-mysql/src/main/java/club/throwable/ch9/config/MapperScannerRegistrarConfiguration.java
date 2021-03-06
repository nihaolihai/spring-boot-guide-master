package club.throwable.ch9.config;

import lombok.NonNull;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/7/19 10:57
 */
@Configuration
public class MapperScannerRegistrarConfiguration {

    public static class AutoConfiguredMapperScannerRegistrar implements
            BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {

        private Environment environment;
        private BeanFactory beanFactory;


        @Override
        public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setEnvironment(@NonNull Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                            @NonNull BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            StringJoiner joiner = new StringJoiner(ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            // 这里使用了${mybatis.mapperPackages},否则会使用AutoConfigurationPackages.get(this.beanFactory)获取项目中自定义配置的包
            String mapperPackages = environment.getProperty("mybatis.mapperPackages");
            if (null != mapperPackages) {
                String[] stringArray = StringUtils.commaDelimitedListToStringArray(mapperPackages);
                for (String pkg : stringArray) {
                    joiner.add(pkg);
                }
            } else {
                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                for (String pkg : packages) {
                    joiner.add(pkg);
                }
            }
            builder.addPropertyValue("basePackage", joiner.toString());
            BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
            Stream.of(beanWrapper.getPropertyDescriptors())
                    .filter(x -> "lazyInitialization".equals(x.getName())).findAny()
                    .ifPresent(x -> builder.addPropertyValue("lazyInitialization",
                            "${mybatis.lazyInitialization:false}"));
            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
        }
    }

    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({MapperFactoryBean.class, MapperScannerConfigurer.class})
    public static class MapperScannerRegistrarNotFoundConfiguration {

    }
}
