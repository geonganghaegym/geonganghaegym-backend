package com.junghaebom.geonganghaegym.schedule.repository.student;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public interface StudentScheduleRepository extends JpaRepository<Schedule, Long>, StudentScheduleRepositoryCustom {

}
