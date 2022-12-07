package com.hmdp.controller;


import com.hmdp.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {

        // 1. 校验时间，库存等信息
        // 2. 事务开始
        // 3. 减卖家库存
        // 4. 增买家资产
        // 5. 创建交易单，状态支付成功
        // 6. 事务完成

        //TODO scs 20221207 和 redis 有啥关系？？？

        return Result.fail("功能未完成");
    }
}
