package com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "merchant_stores")
public class MerchantStoreEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "owner_email", nullable = false, unique = true, length = 160) private String ownerEmail;
    @Column(name = "business_name", nullable = false, length = 160) private String businessName;
    @Column(nullable = false, length = 20) private String ruc;
    @Column(nullable = false, length = 200) private String address;
    @Column(nullable = false, length = 40) private String phone;
    @Column(name = "schedule_week", nullable = false, length = 40) private String scheduleWeek;
    @Column(name = "schedule_sat", nullable = false, length = 40) private String scheduleSat;
    @Column(name = "schedule_sun", nullable = false, length = 40) private String scheduleSun;

    protected MerchantStoreEntity() { }

    public MerchantStoreEntity(Long id, String ownerEmail, String businessName, String ruc, String address,
                               String phone, String scheduleWeek, String scheduleSat, String scheduleSun) {
        this.id = id; this.ownerEmail = ownerEmail; this.businessName = businessName; this.ruc = ruc;
        this.address = address; this.phone = phone; this.scheduleWeek = scheduleWeek;
        this.scheduleSat = scheduleSat; this.scheduleSun = scheduleSun;
    }

    public Long getId() { return id; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getBusinessName() { return businessName; }
    public String getRuc() { return ruc; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getScheduleWeek() { return scheduleWeek; }
    public String getScheduleSat() { return scheduleSat; }
    public String getScheduleSun() { return scheduleSun; }
}
