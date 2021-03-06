package com.market.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.market.constant.ResultCode;
import com.market.dao.GoodDao;
import com.market.exception.ParamsException;
import com.market.model.Good;
import com.market.model.ResultMessage;
import com.market.model.User;
import com.market.service.IGoodService;
import com.market.utils.Base64Util;
import com.market.utils.ValidatorUtil;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liwei
 * @since 2018-04-15
 */
@Service
public class GoodServiceImpl extends ServiceImpl<GoodDao, Good> implements IGoodService {
	@Autowired
	ValidatorUtil<Good> validatorUtil;

	@Override
	public ResultMessage<List<Good>> getAllGood() {
		return new ResultMessage<List<Good>>(true, ResultCode.SUCCESS, "获取成功", selectList(null));
	}

	@Override
	public ResultMessage<Page<Good>> getGoodByPage(Page<Good> page) {
		String keyword = (String)page.getCondition().get("keyword");
		if(keyword != null && keyword != "") {
			page.setCondition(null);
			page = selectPage(page,new EntityWrapper<Good>().like("good_name", keyword));
		} else {
			page.setCondition(null);
			page = selectPage(page);
		}
		List<Good> records = page.getRecords();
		for(int i =0;i<records.size();i++) {
			records.get(i).setFUser(new User().setuId(records.get(i).getuId()).selectById().setPassword(null));
		}
		page.setRecords(records);
		return new ResultMessage<Page<Good>>(true, ResultCode.SUCCESS, "获取成功", page);
	}
	
	@Override
	public ResultMessage<Page<Good>> getGoodByPageByGood(Page<Good> page, Good good) {
		return new ResultMessage<Page<Good>>(true, ResultCode.SUCCESS, "获取成功", selectPage(page,
				new EntityWrapper<Good>().eq("u_id", good.getuId())));
	}
	
	@Override
	public ResultMessage<Page<Good>> getCheckGoodByPage(Page<Good> page) {
		String keyword = (String)page.getCondition().get("keyword");
		Wrapper<Good> wapper = new EntityWrapper<Good>().eq("status", 0);
		if(keyword != null && keyword != "") {
			wapper = wapper.like("good_name", keyword);
		}
		page.setCondition(null);
		page = selectPage(page,wapper);
		List<Good> records = page.getRecords();
		for(int i =0;i<records.size();i++) {
			records.get(i).setFUser(new User().setuId(records.get(i).getuId()).selectById().setPassword(null));
		}
		page.setRecords(records);
		return new ResultMessage<Page<Good>>(true, ResultCode.SUCCESS, "获取成功", page);
	}
	
	@Override
	public ResultMessage<Page<Good>> getPageGoodByClassify(Page<Good> page, Good good) {
		Wrapper<Good> wapper = new EntityWrapper<Good>();
		if(good.getcId() > 0) {
			wapper = wapper.eq("c_id", good.getcId());
		}
		if(good.getGoodName() != null && good.getGoodName() != "") {
			wapper = wapper.like("good_name", good.getGoodName());
		}
		page = selectPage(page,wapper);
		List<Good> records = page.getRecords();
		for(int i =0;i<records.size();i++) {
			System.out.println(records.get(i).getuId());
			records.get(i).setFUser(new User().setuId(records.get(i).getuId()).selectById().setPassword(null));
		}
		page.setRecords(records);
		return new ResultMessage<Page<Good>>(true, ResultCode.SUCCESS, "获取成功", page);
	}

	@Override
	public ResultMessage<Map<String,Object>> getGoodById(Good good) {
		Map<String,Object> result = new HashMap<String,Object>();
		good = selectById(good.getgId());
		result.put("good", good);
		result.put("user", new User().setuId(good.getuId()).selectById().setPassword(null));
		return new ResultMessage<Map<String,Object>>(true, ResultCode.SUCCESS, "获取成功", result);
	}

	@Override
	public ResultMessage<String> addGood(Good good,HttpServletRequest request) throws ParamsException {
		System.out.println(good);
		validatorUtil.validate(good);
		String goodsPhotoUrl = "market_upload/user/" + good.getuId() + "/goods/" + UUID.randomUUID() + ".jpeg";
		String saveUrl = request.getServletContext().getRealPath("/") + "../" + goodsPhotoUrl;
		boolean saveResult = Base64Util.GenerateImage(good.getGoodPics(), saveUrl);
		ResultMessage<String> message;
		if (saveResult) {
			insert(good.setGoodPics(goodsPhotoUrl).setGoodSurplus(good.getGoodCount()));
			message = new ResultMessage<String>(true, ResultCode.SUCCESS, "添加成功", null);
		} else {
			message = new ResultMessage<String>(true, ResultCode.FAIL, "图片上传失败", null);
		}
		return message;
	}

	@Override
	public ResultMessage<String> updateGood(Good good) throws ParamsException {
		validatorUtil.validate(good);
		updateById(good);
		return new ResultMessage<String>(true, ResultCode.SUCCESS, "修改成功", null);
	}

	@Override
	public ResultMessage<String> deleteGood(List<Good> goods) throws ParamsException {
		List<Integer> ids = new ArrayList<>();
		for(Good good : goods) {
			validatorUtil.validate(good);
			ids.add(good.getgId());
		}
		deleteBatchIds(ids);
		return new ResultMessage<String>(true, ResultCode.SUCCESS, "删除成功", null);
	}

	@Override
	public ResultMessage<String> passCheck(Good good) throws ParamsException {
		validatorUtil.validate(good);
		good.setStatus(1);
		updateById(good);
		return new ResultMessage<String>(true, ResultCode.SUCCESS, "通过审核成功", null);
	}

	@Override
	public ResultMessage<String> unpassCheck(Good good) throws ParamsException {
		validatorUtil.validate(good);
		good.setStatus(2);
		updateById(good);
		return new ResultMessage<String>(true, ResultCode.SUCCESS, "拒绝通过审核成功", null);
	}




}
