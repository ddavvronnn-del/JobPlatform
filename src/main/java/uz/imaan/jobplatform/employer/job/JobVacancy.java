package uz.imaan.jobplatform.employer.job;

public class JobVacancy {
    private Long employerChatId;
    private String title;
    private String category;
    private String type;
    private String salary;

    public JobVacancy(Long employerChatId, String title, String category, String type, String salary) {
        this.employerChatId = employerChatId;
        this.title = title;
        this.category = category;
        this.type = type;
        this.salary = salary;
    }

    public Long getEmployerChatId() { return employerChatId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getSalary() { return salary; }
}
