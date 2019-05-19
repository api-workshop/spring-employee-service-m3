package com.example.api.employee;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
    private EmployeeRepository repository;
    private SampleConfig sampleConfig;

    public EmployeeController(EmployeeRepository repository, SampleConfig sampleConfig) {
        this.repository = repository;
        this.sampleConfig = sampleConfig;
    }

    @PostConstruct
    public void init(){
        if(sampleConfig.isInitialize()){
            Employee one = new Employee();
            one.setFirstName(sampleConfig.getFirstNames()[0]);
            one.setLastName(sampleConfig.getLastNames()[0]);
            one.setEmail(sampleConfig.getEmails()[0]);
            repository.save(one);
            Employee two = new Employee();
            two.setFirstName(sampleConfig.getFirstNames()[1]);
            two.setLastName(sampleConfig.getLastNames()[1]);
            two.setEmail(sampleConfig.getEmails()[1]);
            repository.save(two);
        }
    }
    
    @GetMapping("/api/employees")
    public ResponseEntity<List<Employee>> list() {
        List<Employee> response = new ArrayList<>();
        repository.findAll().forEach(response::add);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable(value = "id") final String id) {
        return new ResponseEntity<>(repository.findById(id).get(), HttpStatus.OK);
    }

    @PostMapping("/api/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return new ResponseEntity<>(repository.save(employee), HttpStatus.CREATED);
    }

    @PutMapping("/api/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable(value = "id") final String id, @RequestBody Employee employee) {
        employee.setId(id);
        return new ResponseEntity<>(repository.save(employee), HttpStatus.OK);
    }

    @DeleteMapping("/api/employees/{id}")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") final String id) {
        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}