package com.skillgap.backend.config;

import com.skillgap.backend.entity.CareerRole;
import com.skillgap.backend.entity.RoleSkill;
import com.skillgap.backend.entity.Skill;
import com.skillgap.backend.repository.CareerRoleRepository;
import com.skillgap.backend.repository.RoleSkillRepository;
import com.skillgap.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SkillRepository skillRepo;
    private final CareerRoleRepository roleRepo;
    private final RoleSkillRepository roleSkillRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (skillRepo.count() > 0) {

            return; // already seeded
        }

        Map<String, Skill> s = new HashMap<>();

        add(s, "Java", "Programming");
        add(s, "Spring Boot", "Backend");
        add(s, "SQL", "Database");
        add(s, "MySQL", "Database");
        add(s, "REST APIs", "Backend");
        add(s, "Git", "Tools");
        add(s, "HTML", "Frontend");
        add(s, "CSS", "Frontend");
        add(s, "JavaScript", "Frontend");
        add(s, "React", "Frontend");
        add(s, "Python", "Programming");
        add(s, "Pandas", "Data");
        add(s, "Statistics", "Data");
        add(s, "Excel", "Data");
        add(s, "Data Visualization", "Data");
        add(s, "Machine Learning", "AI");
        add(s, "Manual Testing", "Testing");
        add(s, "Selenium", "Testing");
        add(s, "Test Cases", "Testing");
        add(s, "JUnit", "Testing");
        add(s, "Docker", "DevOps");
        add(s, "Linux", "DevOps");
        add(s, "Data Structures", "Programming");
        add(s, "Problem Solving", "Programming");

        skillRepo.saveAll(s.values());


        prereq(s, "Spring Boot", "Java", "REST APIs");
        prereq(s, "React", "JavaScript");
        prereq(s, "JavaScript", "HTML");
        prereq(s, "Pandas", "Python");
        prereq(s, "Machine Learning", "Python", "Statistics");
        prereq(s, "Selenium", "Java", "Manual Testing");
        prereq(s, "JUnit", "Java");
        prereq(s, "MySQL", "SQL");
        prereq(s, "Data Visualization", "Excel");

        skillRepo.saveAll(s.values());

        role(s, "Full Stack Developer",
            "Builds both the frontend and backend of web applications.",
            new Object[][]{
                {"Java", 4, 0.9}, {"Spring Boot", 4, 0.9}, {"SQL", 3, 0.7},
                {"REST APIs", 4, 0.8}, {"Git", 3, 0.5}, {"HTML", 3, 0.5},
                {"CSS", 3, 0.4}, {"JavaScript", 4, 0.8}, {"React", 4, 0.8},
                {"Data Structures", 3, 0.6}
            });

        role(s, "Data Analyst",
            "Turns raw data into insights using SQL, Python and charts.",
            new Object[][]{
                {"SQL", 4, 0.9}, {"Excel", 4, 0.7}, {"Python", 3, 0.8},
                {"Pandas", 3, 0.8}, {"Statistics", 3, 0.7},
                {"Data Visualization", 4, 0.8}
            });

        role(s, "Software Tester",
            "Finds defects by designing and running manual and automated tests.",
            new Object[][]{

                {"Manual Testing", 4, 0.9}, {"Test Cases", 4, 0.8},
                {"Selenium", 3, 0.8}, {"Java", 3, 0.6}, {"JUnit", 3, 0.6},
                {"SQL", 2, 0.4}, {"Git", 2, 0.3}
            });

        role(s, "Backend Developer",
            "Designs server-side logic, databases and APIs.",
            new Object[][]{
                {"Java", 5, 1.0}, {"Spring Boot", 4, 0.9}, {"SQL", 4, 0.8},
                {"REST APIs", 4, 0.8}, {"Docker", 3, 0.5}, {"Git", 3, 0.5},
                {"Data Structures", 4, 0.7}, {"Linux", 2, 0.3}
            });

        role(s, "Machine Learning Engineer",
            "Builds and trains models that learn from data.",
            new Object[][]{
                {"Python", 4, 0.9}, {"Machine Learning", 4, 1.0},
                {"Statistics", 4, 0.8}, {"Pandas", 3, 0.7},
                {"SQL", 3, 0.4}, {"Git", 2, 0.3}
            });
    }

    private void add(Map<String, Skill> map, String name, String category) {
        Skill k = new Skill();
        k.setName(name);
        k.setCategory(category);
        map.put(name, k);
    }

    private void prereq(Map<String, Skill> map, String skill, String... requires) {
        for (String r : requires) {
            map.get(skill).getPrerequisites().add(map.get(r));

        }
    }

    private void role(Map<String, Skill> map, String title, String desc, Object[][] reqs) {
        CareerRole r = new CareerRole();
        r.setTitle(title);
        r.setDescription(desc);
        roleRepo.save(r);

        for (Object[] q : reqs) {
            RoleSkill rs = new RoleSkill();
            rs.setCareerRole(r);
            rs.setSkill(map.get((String) q[0]));
            rs.setRequiredLevel((Integer) q[1]);
            rs.setWeight((Double) q[2]);
            roleSkillRepo.save(rs);
        }
    }
}
