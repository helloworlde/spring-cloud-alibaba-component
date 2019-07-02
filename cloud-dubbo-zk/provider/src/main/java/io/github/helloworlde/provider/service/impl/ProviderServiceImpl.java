package io.github.helloworlde.provider.service.impl;

import io.github.helloworlde.provider.service.ProviderService;
import org.apache.dubbo.config.annotation.Service;

/**
 * @author HelloWood
 */
@Service
public class ProviderServiceImpl implements ProviderService {

    @Override
    public String sayHello(String name) {
        return "Provider: Hello " + name;
    }
}
