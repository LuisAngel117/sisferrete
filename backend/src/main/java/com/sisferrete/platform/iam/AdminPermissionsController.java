package com.sisferrete.platform.iam;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasAuthority('PERM_IAM_MANAGE')")
public class AdminPermissionsController {
  private final IamService iamService;

  public AdminPermissionsController(IamService iamService) {
    this.iamService = iamService;
  }

  @GetMapping
  public List<IamService.IamPermissionResponse> listPermissions() {
    return iamService.listPermissions();
  }
}
