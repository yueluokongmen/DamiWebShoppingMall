package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Address;
import com.example.mall.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    //获取某个用户的所有收货地址
    // URL: http://localhost:8081/address/list?userId=1
    @GetMapping("/list")
    public Result<List<Address>> list(@RequestParam Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        // 把默认地址排在最前面 (isDefault降序: 1 在 0 前面)
        wrapper.orderByDesc(Address::getIsDefault);

        List<Address> list = addressMapper.selectList(wrapper);
        return Result.success(list);
    }

    //新增收货地址
    // URL: POST http://localhost:8081/address/add
    @PostMapping("/add")
    public Result<String> add(@RequestBody Address address) {
        // 如果是该用户的第一个地址，自动设为默认
        LambdaQueryWrapper<Address> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Address::getUserId, address.getUserId());
        if (addressMapper.selectCount(countWrapper) == 0) {
            address.setIsDefault(1);
        } else {
            // 否则默认是非默认
            if(address.getIsDefault() == null) address.setIsDefault(0);
        }

        addressMapper.insert(address);
        return Result.success("添加成功");
    }
    // 修改地址
    // URL: POST http://localhost:8081/address/update
    @PostMapping("/update")
    public Result<String> update(@RequestBody Address address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Address::getUserId, address.getUserId())
                    .set(Address::getIsDefault, 0); // 全部置为0
            addressMapper.update(null, wrapper);
        }

        addressMapper.updateById(address);
        return Result.success("修改成功");
    }

    // 删除地址
    // URL: DELETE http://localhost:8081/address/delete/10
    @DeleteMapping("/delete/{addressId}")
    public Result<String> delete(@PathVariable Long addressId) {
        addressMapper.deleteById(addressId);
        return Result.success("删除成功");
    }


}