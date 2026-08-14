package uz.imaan.jobplatform.employer.state;

public enum EmployerState {
        MAIN_MENU,
        SETTINGS_MENU,
        WAITING_FOR_LANGUAGE,

        // Vakansiya yaratish bosqichlari
        WAITING_FOR_TITLE,
        WAITING_FOR_CATEGORY,
        WAITING_FOR_CUSTOM_CATEGORY,
        WAITING_FOR_TYPE,
        WAITING_FOR_WORK_HOURS,
        WAITING_FOR_WORKER_COUNT,
        WAITING_FOR_SALARY,
        WAITING_FOR_REQUIREMENTS,
        WAITING_FOR_PHONE,
    NONE, WAITING_FOR_JOB_TYPE
}