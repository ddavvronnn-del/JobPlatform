package uz.imaan.jobplatform.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "test", url = "https://jsonplaceholder.typicode.com")
public interface TestFeignClient {

    @GetMapping(value = "/posts/1")
    String getData();
}
