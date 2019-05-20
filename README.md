# spring-employee-service-m3

Now, lets add PAS client dependencies

## Add PAS Client Dependencies

pom.xml

```xml
    ...

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud-services.version>1.6.6.RELEASE</spring-cloud-services.version>
        <spring-cloud.version>Greenwich.RELEASE</spring-cloud.version>		
  	</properties>    

    ...

    <dependencies>

    ...

    <!-- Spring Cloud (PCF) Dependencies -->
      <dependency>
          <groupId>io.pivotal.spring.cloud</groupId>
          <artifactId>spring-cloud-services-starter-config-client</artifactId>
      </dependency>
      <dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-security</artifactId>
  	  </dependency>

	  <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
	  </dependency>
	  
    </dependencies>

    ...

    <!-- Add Cloud Service Starters -->
	<!-- Add Cloud Service Starters -->
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.pivotal.spring.cloud</groupId>
				<artifactId>
					spring-cloud-services-dependencies
				</artifactId>
				<version>${spring-cloud-services.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

  ...
```

## Let's prime the DB with some test data on startup

1. Create a class named `SampleConfig`
2. Annotate the class with `@Data` and `@Configuration("sample")`
3. Declare the following member variables `boolean initialize`, `String[] firstNames`, `String[] lastNames`, `String[] emails`

After adding code the class should look like below,

![Local Image](/assets/sample-config.PNG)

## Update the Controller to use Sample Configs

Code for `EmployeeController` is provided below,

src/main/java/com/example/api/employee/EmployeeController.java

```java
package com.example.api.employee;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
        return new ResponseEntity<>(repository.findOne(id), HttpStatus.OK);
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
        repository.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
```
## Add Security Configuration

1. Create a class named `SecurityConfiguration` which extends `WebSecurityConfigurerAdapter`
2. Annotate the class with `@Configuration`
3. Override the `configure` method as below (_to permit all requests_)

```java
package com.example.api.employee;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/", "/api/**/*", "/employee/**/*").permitAll();
	}
}
```

## Update manifest file

Update `manifest.yml` to add `config-service` and `discovery-service` which we created earlier

```yaml
applications:
  - buildpacks:
      - https://github.com/cloudfoundry/java-buildpack.git
    name: spring-employees
    memory: 2048m
    path: target/employee-0.0.1-SNAPSHOT.jar
    random-route: true
    services:
      - mysql-service
      - config-service
    timeout: 180

```

## Build & Push the app

```
./mvnw clean package
```
```
cf push
```
