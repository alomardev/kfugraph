package com.alomardev.kfugraph;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    
    private static final String INITIAL_GRAPH = "مجهول,20,32;مجهول,42,91;كلية الطب,75,104;كلية الصيدلة الإكلينيكية,127,144;كلية الدراسات التطبيقية,167,99;المعامل المركزية للطالبات,178,120;مبنى الطالبات 2,179,140;مجهول,138,194;مبنى الطالبات 3,198,173;مبنى الطالبات 1,211,118;مبنى القاعات المركزية,209,98;مبنى الدوائر التلفزيونية,227,93;كلية العلوم الزراعية والأغذية,280,134;كلية العلوم الزراعية,306,130;كلية الصيدلة الإكلينيكية,294,160;مبنى القاعات الدراسية,306,190;إدارة وحدات الصيانة,261,65;إدارة الخدمات العامة,283,88;إدارة الحركة,313,87;إدارة المستودعات,324,55;إدارة مطبعة الجامعة,343,67;إدارة المستودعات,345,47;كلية العلوم الإدارية والتخطيط,369,74;إدارة المدينة الجامعية,351,20;مباني ضيافة الجامعة,398,43;قاعة الشيخ حسن آل الشيخ,403,62;مركز تقنية المعلومات,455,59;ملعب كرة القدم,444,134;الملاعب,386,122;إدارة المشاريع والخدمات العامة,334,113;المسجد,345,127;إدارة الجامعة,339,156;مركز اللغة الإنجليزية,340,182;عمادة القبول والتسجيل,352,194;مطعم الطلاب,341,226;إدارة الحدائق,278,230;عمادة شؤون الطلاب,295,249;المكتبة المركزية,326,285;سكيكو الضغط العالي,333,386;بنك الرياض,384,354;المركز الصحي,431,269;كلية علوم الحاسب وتقنية المعلومات,458,190;كلية العلوم,384,251;المعامل المركزية للطلاب,382,286;كلية الدراسات التطبيقية,418,188;الصالة الرياضية,395,146;إدارة الأمن والسلامة,282,100;بوابة 1,487,80;بوابة 2,488,231;بوابة 3,288,329;بوابة 4,261,204;بوابة 5,106,177|1,2,197.49;2,3,111.24;3,4,205.76;3,5,288.97;52,8,113.65;52,4,122.68;8,4,160.57;8,9,199.38;4,7,163.57;7,6,62.81;6,5,74.35;5,11,131.77;11,12,58.59;10,11,63.04;10,6,103.69;7,9,119.43;12,17,138.14;17,18,99.82;18,47,37.77;17,19,177.09;19,20,106.13;20,21,70.48;21,22,63.04;20,22,70.48;22,24,86.75;24,25,164.11;25,26,61.62;26,27,163.36;48,27,120.05;48,28,216.5;28,42,181.04;49,42,159.34;42,45,125.61;28,46,158.22;21,23,84.45;26,23,113.08;25,23,133.14;23,29,159.71;29,46,80.39;48,29,343.07;30,31,55.84;13,15,92.62;15,16,101.34;32,31,92.88;18,30,178.14;47,13,106.82;19,30,104.82;46,45,150.19;33,34,53.23;32,33,81.61;51,16,147.81;51,15,172.5;51,36,97.43;36,35,197.99;36,37,79.96;37,38,149.0;50,38,182.34;50,39,227.77;39,40,188.84;40,44,213.37;44,41,162.67;38,44,175.67;44,43,109.95;43,41,157.85;41,49,214.86;45,43,224.53;43,35,156.0;35,34,106.13;35,16,157.48;35,38,190.93;35,45,269.31;14,31,122.68;14,13,82.51;33,46,206.17;33,16,109.55;9,51,220.22;13,9,284.79|508,406";

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
            InstantiationException |
            IllegalAccessException |
            UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        GraphSketch gs = new GraphSketch();
        GraphFrame gframe = new GraphFrame(gs);
        gframe.setVisible(true);
        
        gs.parseGraph(INITIAL_GRAPH);
    }
}
