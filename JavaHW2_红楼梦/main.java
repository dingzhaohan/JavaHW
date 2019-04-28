import java.io.*;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import java.util.List;

public class main {

    static String[] word_dic = {"之", "其", "或", "亦", "方", "于", "即", "皆", "因", "仍", "故", "尚", "呢", "了", "的",
                                "着", "一", "不", "乃", "呀", "吗", "咧", "啊", "把", "让", "向", "往", "是", "在", "越",
                                "再", "更", "比", "很", "偏", "别", "好", "可", "便", "就", "管", "嘴", "但", "儿", "又",
                                "也", "都", "要", "这", "那", "你", "我", "他", "来", "去", "道", "说", "往",
                                };

    public static void build_arff_data(String str, String name) {
        try {
            int len = word_dic.length;
            //创建训练集arff文件
            FileWriter fw = new FileWriter(name + ".arff");
            fw.append("@relation guess_label\n");
            for(int k = 1; k <= len; ++k) {
                fw.append("@attribute feature" + k  + " numeric" + '\n');
            }
            fw.append("@attribute label{0, 1, 2}\n@data\n");

            //读入训练集地址
            BufferedReader br = new BufferedReader(new FileReader(str));
            String message = br.readLine();
            while(message != null) {
                //统计词频
                int[] word_frequency = new int[len];
                String TextPath;
                String Label = "";
                if(name.equals("training")) {
                    TextPath = message.substring(2);
                    Label = message.substring(0,1);
                }
                else {
                    TextPath = message;
                }
                message = br.readLine();
                BufferedReader br1 = new BufferedReader(new FileReader(TextPath));
                String line = "";

                //创建分词器
                Segment segment = new NShortSegment();
                //关闭词性显示
                HanLP.Config.ShowTermNature = false;
                List<Term> termList = null;
                while(line != null) {
                    try {
                        line = br1.readLine();
                        //分词
                        termList = segment.seg(line);
                        for(int j = 0; j < termList.size(); ++j) {
                            String sss = termList.get(j).toString().trim();
                            for(int k = 0; k < word_dic.length; ++k) {
                                if(sss.equals(word_dic[k])) {
                                    //找到是词典中的哪一个，对应数字+1
                                    word_frequency[k]++;
                                }
                            }
                        }
                    } catch (Exception e) {}
                }

                //把这一回合的结果作为一个instance写入arff文件，最后一个值代表类别
                for(int k = 0; k < word_frequency.length; ++k) {
                    fw.append(word_frequency[k] + ",");
                }
                if(name.equals("training")) {
                    fw.append(Label + '\n');
                }
                else {
                    fw.append("?\n");
                }
                fw.flush();
            }
        } catch(Exception e) {}
    }

    public static void main(String []args) {

        String TrainingSetPath = args[0];
        String TestSetPath = args[1];
        //创建分类器
        Classifier classifier = new NaiveBayes();
        //--------------------------------读入训练数据、构建training.arff--------------------------------------------
        build_arff_data(TrainingSetPath, "training");
        //构建weka实例
        try {
            Instances weka_data = new Instances(new BufferedReader(new FileReader("training.arff")));
            //设置最后一个属性为类别
            weka_data.setClassIndex(weka_data.numAttributes() - 1);
            classifier.buildClassifier(weka_data);
        } catch(Exception e) {}

        //-------------------------------------读入测试数据、构建testing.arff----------------------------------------
        build_arff_data(TestSetPath, "testing");
        //--------------------------------------------prediction---------------------------------------------------

        try {
            Instances weka_test_data = new Instances(new BufferedReader(new FileReader("testing.arff")));
            //设置最后一个属性为类别
            weka_test_data.setClassIndex(weka_test_data.numAttributes() - 1);
            FileWriter fw = new FileWriter("[java19]HW2_1700017720.txt");
            Evaluation evaluation = new Evaluation(weka_test_data);
            for(int i = 0; i < weka_test_data.size(); ++i) {
                double predictvalue = evaluation.evaluateModelOnceAndRecordPrediction(classifier,weka_test_data.instance(i));
                fw.append((int)predictvalue+"\n");
                fw.flush();
            }
        }catch (Exception e) {}
    }
}



