package com.efintechb.lbsite.statistics.repository;

import com.efintechb.lbsite.statistics.model.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, Long> {

    @Query("select count(dr) from DeviceRegistration dr where upper(dr.deviceType) = upper(:deviceType)")
    long countByDeviceTypeIgnoreCase(@Param("deviceType") String deviceType);
}
