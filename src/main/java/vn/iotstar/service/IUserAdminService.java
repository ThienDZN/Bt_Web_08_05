package vn.iotstar.service;

import java.util.List;

import vn.iotstar.entity.UserAccount;

public interface IUserAdminService {
    List<UserAccount> findAll();

    List<UserAccount> search(String keyword);

    UserAccount findById(Long userId);

    void insert(UserAccount user, String rawPassword);

    UserAccount update(UserAccount user, String rawPassword);

    void delete(Long userId) throws Exception;
}
