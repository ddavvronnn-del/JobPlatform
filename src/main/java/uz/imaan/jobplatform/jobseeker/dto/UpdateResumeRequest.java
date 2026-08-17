package uz.imaan.jobplatform.jobseeker.dto;



public record UpdateResumeRequest (

     String title,           // Yangi sarlavha
     String fullName,        // Yangi ism
     String email,           // Yangi email
     String phoneNumber,     // Yangi telefon
     String profession,      // Yangi kasb
     String experience,      // Yangi tajriba
     String education,       // Yangi ta'lim
     String skills,          // Yangi ko'nikmalar
     String about           // Yangi "o'zim haqimda"

){}
