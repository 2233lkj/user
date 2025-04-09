package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.dao.AuthenticationDao;
import com.springboot.logindemo.dao.EnterpriseDao;
import com.springboot.logindemo.dao.IndividualBusinessDao;
import com.springboot.logindemo.dao.NaturalPersonDao;
import com.springboot.logindemo.domain.Authentication;
import com.springboot.logindemo.domain.Enterprise;
import com.springboot.logindemo.domain.IndividualBusiness;
import com.springboot.logindemo.domain.NaturalPerson;
import com.springboot.logindemo.dto.AuthenticationDto;
import com.springboot.logindemo.service.AuthenticationService;
import com.springboot.logindemo.service.UserService;
import com.springboot.logindemo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author 认证服务实现类
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private AuthenticationDao authenticationDao;

    @Autowired
    private NaturalPersonDao naturalPersonDao;

    @Autowired
    private IndividualBusinessDao individualBusinessDao;

    @Autowired
    private EnterpriseDao enterpriseDao;

    @Autowired
    private UserService userService;

    @Value("${file.upload.path}")
    private String fileUploadPath;

    @Value("${file.access.path}")
    private String fileAccessPath;

    @Override
    @Transactional
    public Map<String, Object> submitAuthentication(AuthenticationDto authDto) {
        // 验证token
        if (!JwtUtils.validateToken(authDto.getToken())) {
            throw new RuntimeException("无效的token");
        }
        String uid = JwtUtils.getUserIdFromToken(authDto.getToken());

        // 创建认证记录
        Authentication authentication = new Authentication();
        authentication.setUserId(Long.parseLong(uid));
        authentication.setAuthType(authDto.getAuthType());
        authentication.setAuthMethod(authDto.getAuthMethod());
        authentication.setAuthStatus(0); // 待审核
        authentication = authenticationDao.save(authentication);

        // 根据认证类型保存不同的信息
        if ("natural".equals(authDto.getAuthType())) {
            saveNaturalPerson(authentication.getId(), authDto);
        } else if ("individual".equals(authDto.getAuthType())) {
            saveIndividualBusiness(authentication.getId(), authDto);
        } else if ("enterprise".equals(authDto.getAuthType())) {
            saveEnterprise(authentication.getId(), authDto);
        } else {
            throw new RuntimeException("不支持的认证类型");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("authId", authentication.getId());
        result.put("authType", authentication.getAuthType());
        result.put("authStatus", authentication.getAuthStatus());
        result.put("createTime", authentication.getCreateTime());

        return result;
    }

    @Override
    public Map<String, Object> getAuthenticationStatus(String token) {
        // 验证token
        if (!JwtUtils.validateToken(token)) {
            throw new RuntimeException("无效的token");
        }
        String uid = JwtUtils.getUserIdFromToken(token);

        // 查询用户的认证记录
        List<Authentication> authentications = authenticationDao.findByUserId(Long.parseLong(uid));

        Map<String, Object> result = new HashMap<>();
        if (authentications.isEmpty()) {
            result.put("hasAuthentication", false);
        } else {
            // 获取最新的认证记录
            Authentication latestAuth = authentications.stream()
                    .max((a1, a2) -> a1.getCreateTime().compareTo(a2.getCreateTime()))
                    .get();

            result.put("hasAuthentication", true);
            result.put("authId", latestAuth.getId());
            result.put("authType", latestAuth.getAuthType());
            result.put("authStatus", latestAuth.getAuthStatus());
            result.put("createTime", latestAuth.getCreateTime());

            // 根据认证类型获取详细信息
            if ("natural".equals(latestAuth.getAuthType())) {
                NaturalPerson naturalPerson = naturalPersonDao.findByAuthId(latestAuth.getId());
                if (naturalPerson != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", naturalPerson.getName());
                    details.put("idCard", naturalPerson.getIdCard());
                    details.put("phone", naturalPerson.getPhone());
                    details.put("promiseFile", naturalPerson.getPromiseFile());
                    details.put("delegateFile", naturalPerson.getDelegateFile());
                    result.put("details", details);
                }
            } else if ("individual".equals(latestAuth.getAuthType())) {
                IndividualBusiness individualBusiness = individualBusinessDao.findByAuthId(latestAuth.getId());
                if (individualBusiness != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", individualBusiness.getName());
                    details.put("taxNo", individualBusiness.getTaxNo());
                    details.put("address", individualBusiness.getAddress());
                    details.put("licenseFile", individualBusiness.getLicenseFile());
                    result.put("details", details);
                }
            } else if ("enterprise".equals(latestAuth.getAuthType())) {
                Enterprise enterprise = enterpriseDao.findByAuthId(latestAuth.getId());
                if (enterprise != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", enterprise.getName());
                    details.put("taxNo", enterprise.getTaxNo());
                    details.put("address", enterprise.getAddress());
                    details.put("licenseFile", enterprise.getLicenseFile());
                    result.put("details", details);
                }
            }
        }

        return result;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileType) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 生成文件存储路径
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 确保目录存在
            String directoryPath = fileUploadPath + File.separator + fileType + File.separator + dateFolder;
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 保存文件
            Path filePath = Paths.get(directoryPath, newFileName);
            Files.write(filePath, file.getBytes());

            // 返回文件访问路径
            return fileAccessPath + "/" + fileType + "/" + dateFolder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    // 保存自然人认证信息
    private void saveNaturalPerson(Long authId, AuthenticationDto authDto) {
        NaturalPerson naturalPerson = new NaturalPerson();
        naturalPerson.setAuthId(authId);
        naturalPerson.setName(authDto.getName());
        naturalPerson.setIdCard(authDto.getIdCard());
        naturalPerson.setPhone(authDto.getPhone());
        naturalPerson.setBankCard(authDto.getBankCard());

        // 上传文件
        if (authDto.getPromiseFile() != null && !authDto.getPromiseFile().isEmpty()) {
            naturalPerson.setPromiseFile(uploadFile(authDto.getPromiseFile(), "promise"));
        }

        if (authDto.getDelegateFile() != null && !authDto.getDelegateFile().isEmpty()) {
            naturalPerson.setDelegateFile(uploadFile(authDto.getDelegateFile(), "delegate"));
        }

        naturalPersonDao.save(naturalPerson);
    }

    // 保存个体工商户认证信息
    private void saveIndividualBusiness(Long authId, AuthenticationDto authDto) {
        IndividualBusiness individualBusiness = new IndividualBusiness();
        individualBusiness.setAuthId(authId);
        individualBusiness.setName(authDto.getBusinessName());
        individualBusiness.setTaxNo(authDto.getTaxNo());
        individualBusiness.setAddress(authDto.getAddress());

        // 上传文件
        if (authDto.getLicenseFile() != null && !authDto.getLicenseFile().isEmpty()) {
            individualBusiness.setLicenseFile(uploadFile(authDto.getLicenseFile(), "license"));
        }

        individualBusinessDao.save(individualBusiness);
    }

    // 保存企业法人认证信息
    private void saveEnterprise(Long authId, AuthenticationDto authDto) {
        Enterprise enterprise = new Enterprise();
        enterprise.setAuthId(authId);
        enterprise.setName(authDto.getBusinessName());
        enterprise.setTaxNo(authDto.getTaxNo());
        enterprise.setAddress(authDto.getAddress());

        // 上传文件
        if (authDto.getLicenseFile() != null && !authDto.getLicenseFile().isEmpty()) {
            enterprise.setLicenseFile(uploadFile(authDto.getLicenseFile(), "license"));
        }

        enterpriseDao.save(enterprise);
    }

    // 获取文件扩展名
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
}