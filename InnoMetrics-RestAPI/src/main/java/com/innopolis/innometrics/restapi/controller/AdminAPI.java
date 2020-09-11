package com.innopolis.innometrics.restapi.controller;

import com.innopolis.innometrics.restapi.DTO.*;
import com.innopolis.innometrics.restapi.config.JwtToken;
import com.innopolis.innometrics.restapi.entitiy.*;
import com.innopolis.innometrics.restapi.exceptions.ValidationException;
import com.innopolis.innometrics.restapi.repository.MeasurementTypeRepository;
import com.innopolis.innometrics.restapi.repository.ProjectRepository;
import com.innopolis.innometrics.restapi.repository.RoleRepository;
import com.innopolis.innometrics.restapi.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT})
@RequestMapping(value = "/V1/Admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminAPI {

    private static Logger LOG = LogManager.getLogger();

    @Autowired
    private JwtToken jwtTokenUtil;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    @Autowired
    ProjectRepository projectService;

    @Autowired
    AdminService adminService;

    @Autowired
    MeasurementTypeRepository measurementTypeService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    ProfileService profileService;

    @GetMapping("/Role/Permissions/{RoleName}")
    public ResponseEntity<List<Page>> ListRolePermissions(@PathVariable String RoleName) {

        if (roleService.getRole(RoleName)== null) {
            throw new ValidationException("No such role");
        }

        List<Page> lTemp = roleService.getPagesWithIconsForRole(RoleName);
        if (lTemp.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(lTemp, HttpStatus.OK);
    }

    @GetMapping("/Role/{RoleName}")
    public ResponseEntity<RoleResponse> GetRoleByName(@PathVariable String RoleName, @RequestHeader(required = false) String Token) {
        RoleResponse role = roleService.getRole(RoleName);
        if (role == null) {
            throw new ValidationException("The role doesn't exist");
        }

        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PostMapping("/Role")
    public ResponseEntity<RoleResponse> CreateRole(@RequestBody RoleRequest roleRequest, @RequestHeader(required = false) String Token) {
        if (roleRequest == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (roleService.getRole(roleRequest.getName()) != null) {
            throw new ValidationException("The role already existed");
        }

        if (roleRequest.getName() == null || roleRequest.getPages() == null || roleRequest.getDescription() == null ) {
            throw new ValidationException("Not enough data provided");
        }

        RoleResponse myRole = roleService.createRole(roleRequest);


        return new ResponseEntity<>(myRole, HttpStatus.CREATED);
    }

    @PostMapping("/Role/Permissions")
    public ResponseEntity<PermissionResponse> CreatePermissions(@RequestBody PermissionResponse permissionResponse, @RequestHeader(required = false) String Token) {
        if (permissionResponse == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (roleService.getRole(permissionResponse.getRole()) == null) {
            throw new ValidationException("The role does not exist");
        }


        PermissionResponse permissionResponseOut = permissionService.createPermissios(permissionResponse);

        return new ResponseEntity<>(permissionResponseOut, HttpStatus.CREATED);
    }


    @PutMapping("/Role")
    public ResponseEntity<RoleResponse> UpdateRole(@RequestBody RoleRequest roleRequest, @RequestHeader(required = false) String Token) {
        if (roleRequest == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (roleRequest.getName() == null || roleRequest.getPages() == null || roleRequest.getDescription() == null ) {
            throw new ValidationException("Not enough data provided");
        }

        if (roleService.getRole(roleRequest.getName()) == null) {
            RoleResponse myRole = roleService.createRole(roleRequest);
            return new ResponseEntity<>(myRole, HttpStatus.CREATED);
        }

        RoleResponse myRole = roleService.updateRole(roleRequest);

        return ResponseEntity.ok(myRole);
    }


    @GetMapping("/Role")
    public ResponseEntity<RoleListResponse> ListAllRoles(@RequestHeader(required = false) String Token) {
        RoleListResponse lTemp = roleService.getRoles();
        if (lTemp.getRoleList().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(lTemp, HttpStatus.OK);
    }


    @GetMapping("/User/Permissions/{UserName}")
    public ResponseEntity<PermissionResponse> GetPermissionsOfUser(@PathVariable String UserName, @RequestHeader(required = false) String Token) {

        if (!userService.existsByEmail(UserName)) {
            throw new ValidationException("Username does not existed");
        }
        PermissionResponse permissionResponse = permissionService.getPermissiosOfUser(UserName);

        return new ResponseEntity<>(permissionResponse, HttpStatus.OK);

    }


    @PostMapping("/User/Role")
    public ResponseEntity<UserResponse> SetRoleOfUser(@RequestParam String UserName, @RequestParam String RoleName , @RequestHeader(required = false) String Token) {

        if (!userService.existsByEmail(UserName)) {
            throw new ValidationException("Username does not existed");
        }

        if(roleService.getRole(RoleName) == null){
            throw new ValidationException("No such role");
        }

        UserResponse userResponse = userService.setRole(UserName, RoleName);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);

    }



    @PostMapping("/User")
    public ResponseEntity<UserRequest> CreateUser(@RequestBody UserRequest user, @RequestHeader(required = false) String Token) {
        if (user == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (user.getEmail() == null || user.getName() == null || user.getSurname() == null || user.getPassword() == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (userService.existsByEmail(user.getEmail())) {
            throw new ValidationException("Username already existed");
            //return new ResponseEntity<>("Username already existed", HttpStatus.FOUND);
        }

        if (roleService.getRole(user.getRole())== null) {
            throw new ValidationException("No such role");
        }

        String UserName = "API";

        if (Token != null) {
            UserName = jwtTokenUtil.getUsernameFromToken(Token);
        }

        userService.create(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);

    }

    @PutMapping("/User/UpdateStatus")
    public ResponseEntity<Boolean> UpdateUserStatus(@RequestParam String UserId, @RequestParam Boolean IsActive, @RequestHeader String Token) {
        if (UserId == null) {
            throw new ValidationException("Not enough data provided");
        }

        User myUser = userService.findByEmail(UserId);

        if (myUser == null) {
            throw new ValidationException("Username doesn't");
            //return new ResponseEntity<>("Username already existed", HttpStatus.FOUND);
        }

        String UserName = jwtTokenUtil.getUsernameFromToken(Token);

        myUser.setIsactive(IsActive ? "Y" : "N");
        myUser.setLastupdate(new Date());
        myUser.setUpdateby(UserName);

        Boolean response = userService.update(myUser) != null;

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    //Add project
    @PostMapping("/Project")
    public ResponseEntity<ProjectResponse> CreateProject(@RequestBody ProjectRequest project, @RequestHeader(required = false) String Token) {
        ProjectResponse response = adminService.CreateProject(project, Token);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/Project")
    public ResponseEntity<ProjectResponse> updateProject(@RequestBody ProjectRequest project, @RequestHeader(required = true) String Token) {
        ProjectResponse response = adminService.updateProject(project, Token);
        return ResponseEntity.ok(response);
    }

    //Invite user in a project
    @PostMapping("/project/{ProjectName}")
    public ResponseEntity<Boolean> InviteUserProject(@PathVariable String ProjectName,
                                                     @RequestParam String UserEmail,
                                                     @RequestParam Boolean Manager,
                                                     @RequestHeader String Token) {
        if (ProjectName == null || UserEmail == null) {
            throw new ValidationException("Not enough data provided");
        }

        Project myProject = projectService.findByName(ProjectName);
        if (myProject == null) {
            throw new ValidationException("Project doesn't exist");
        }

        User myUser = userService.findByEmail(UserEmail);
        if (myUser == null) {
            throw new ValidationException("User doesn't exist");
        }

        //myProject.ge(name);
        // add user to manager or user list
        projectService.save(myProject);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }


    @PostMapping("/MeasurementType")
    public ResponseEntity<MeasurementTypeResponse> CreateMeasurementType(@RequestBody MeasurementTypeRequest measurementType,
                                                                         @RequestHeader String Token) {

        if (measurementType == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (measurementType.getLabel() == null || measurementType.getWeight() == null) {
            throw new ValidationException("Not enough data provided");
        }

        if (measurementTypeService.existsByLabel(measurementType.getLabel())) {
            throw new ValidationException("Measurement type already existed");
        }

        String UserName = jwtTokenUtil.getUsernameFromToken(Token);


        MeasurementType myType = new MeasurementType();
        myType.setLabel(measurementType.getLabel());
        myType.setDescription(measurementType.getDescription());
        myType.setWeight(measurementType.getWeight());
        myType.setOperation(measurementType.getOperation());
        myType.setScale(measurementType.getScale());
        myType.setIsactive("Y");
        myType.setCreatedby(UserName);
        myType.setCreationdate(new Date());

        measurementTypeService.save(myType);

        MeasurementTypeResponse response = new MeasurementTypeResponse(myType);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/Project")
    public ResponseEntity<ProjectListResponse> getActiveProjects() {
        ProjectListResponse response = adminService.getActiveProjects();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Users")
    public ResponseEntity<UserListResponse> getActiveUsers(@RequestParam(required = false) String ProjectId) {
        UserListResponse response = adminService.getActiveUsers(ProjectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Users/projects/{UserName}")
    public ResponseEntity<ProjectListResponse> getProjectsByUsername(@PathVariable String UserName,@RequestHeader String Token) {

        ProjectListResponse response = adminService.getProjectsByUsername(UserName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Classification/Category")
    public ResponseEntity<CategoryListResponse> getAllCategories(@RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        CategoryListResponse response = categoryService.getAllCategories(Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/Classification/Category")
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest,
                                                        UriComponentsBuilder ucBuilder,
                                                        @RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        CategoryResponse response = categoryService.addCategory(categoryRequest, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/Classification/Category/{CategoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer CategoryId, @RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        CategoryResponse response = categoryService.getCategoryById(CategoryId, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/Classification/Category")
    public ResponseEntity<CategoryResponse> UpdateCategory(@RequestBody CategoryRequest categoryRequest,
                                                           UriComponentsBuilder ucBuilder,
                                                           @RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        CategoryResponse response = categoryService.UpdateCategory(categoryRequest, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/Classification/App")
    public ResponseEntity<AppCategoryResponse> addAppCategory(@RequestBody AppCategoryRequest appCategoryRequest,
                                                              UriComponentsBuilder ucBuilder,
                                                              @RequestHeader(required = false) String Token) {


        if(Token == null) Token = "";
        AppCategoryResponse response = categoryService.addAppCategory(appCategoryRequest, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/Classification/App/{AppId}")
    public ResponseEntity<AppCategoryResponse> getAppCategoryById(@PathVariable Integer AppId, @RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        AppCategoryResponse response = categoryService.getAppCategoryById(AppId, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/Classification/App")
    public ResponseEntity<AppCategoryResponse> UpdateAppCategory(@RequestBody AppCategoryRequest appCategoryRequest,
                                                                 UriComponentsBuilder ucBuilder,
                                                                 @RequestHeader(required = false) String Token) {

        if(Token == null) Token = "";
        AppCategoryResponse response = categoryService.UpdateAppCategory(appCategoryRequest, Token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/User/Profile")
    public ResponseEntity<ProfileRequest> updateProfileOfUser(@RequestBody ProfileRequest profileRequest, @RequestHeader(required = false) String Token){
        return new ResponseEntity<>(
                profileService.updateProfileOfUser(profileRequest,Token),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/User/Profile")
    public boolean deleteProfile(@RequestParam Integer id, @RequestHeader(required = false) String token) {
        return profileService.deleteProfile(id,token);

    }

    @GetMapping("User/Profile")
    public ProfileRequest findByMacaddress(@RequestParam String macaddress, @RequestHeader(required = false) String token) {

        return profileService.findByMacaddress(macaddress,token);
    }




//    @DeleteMapping("/User/Profile")
//    public ResponseEntity<ProfileRequest> deleteProfile(@RequestParam Integer id, @RequestHeader(required = false) String Token) {
//        //change later to required = true and delete this line
//        String email = "";
//        if (Token != null)
//            email = jwtTokenUtil.getUsernameFromToken(Token);
//
//        profileService.delete(id);
//
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("User/Profile")
//    public ResponseEntity<ProfileRequest> findByMacaddress(@RequestParam String macaddress, @RequestHeader(required = false) String Token) {
//        //change later to required = true and delete this line
//        String email = "";
//        if (Token != null)
//            email = jwtTokenUtil.getUsernameFromToken(Token);
//
//
//        return ResponseEntity.ok(
//                profileService.findByMacAddress(macaddress)
//        );
//    }


    //accept invitation

    //Load user from request

}
