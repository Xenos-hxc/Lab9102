-- Synthetic local demo only. Do not reuse this password outside the demo.
INSERT INTO member (name,identity,username,password,isAdmin,adminPermissions) VALUES ('演示管理员','负责人','admin','local-demo-change-me',1,'*');
INSERT INTO labinfo (lab_name,address_province,address_city,address_district,address_street,address_detail,university,college,contact_person1_name,contact_person1_email,introduction) VALUES ('Lab9102 演示实验室','演示省','演示市','演示区','演示街道','演示地址','示例大学','示例学院','演示联系人','contact@example.com','实验室网站功能演示，所有账号与联系信息均为虚构。');
INSERT INTO forum_category (name,description,sort_order) VALUES ('学术交流','演示讨论分类',1);
INSERT INTO equipment_category (name,description,sortOrder) VALUES ('实验平台','演示设备分类',1);
