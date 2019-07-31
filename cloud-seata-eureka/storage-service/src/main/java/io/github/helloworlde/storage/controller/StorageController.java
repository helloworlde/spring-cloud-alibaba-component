package io.github.helloworlde.storage.controller;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.storage.model.ReduceStockRequestVO;
import io.github.helloworlde.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/storage")
@Slf4j
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/reduceStock")
    @ResponseBody
    public OperationResponse reduceStock(@RequestBody ReduceStockRequestVO reduceStockRequestVO) throws Exception {
        return storageService.reduceStock(reduceStockRequestVO);
    }
}
