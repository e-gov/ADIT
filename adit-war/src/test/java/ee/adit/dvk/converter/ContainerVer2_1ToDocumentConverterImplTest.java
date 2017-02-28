package ee.adit.dvk.converter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import ee.adit.dvk.api.container.v2_1.ContainerVer2_1;
import ee.adit.dvk.api.container.v2_1.DecSender;
import ee.adit.dvk.api.container.v2_1.File;
import ee.adit.dvk.api.container.v2_1.Transport;
import ee.adit.dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.DigiDocExtractionResult;

/**
 * @author Hendrik Pärna
 * @since 10.06.14
 */
public class ContainerVer2_1ToDocumentConverterImplTest {

    @Test
    public void testSimpleConversion() {
        PojoMessage message = new PojoMessage();

        ContainerVer2_1ToDocumentConverterImpl converter = new ContainerVer2_1ToDocumentConverterImpl(message);
        converter.setAditUserDAO(new AditUserDAO() {
            @Override
            public AditUser getUserByID(String userCode) {
                AditUser aditUser = new AditUser();
                aditUser.setUserCode("3832828");
                return aditUser;
            }
        });

        DocumentService documentService = new DocumentService();
        converter.setDocumentService(documentService);

        ContainerVer2_1 container = new ContainerVer2_1();
        Transport transport = new Transport();
        DecSender sender = new DecSender();
        sender.setOrganisationCode("12312");
        sender.setPersonalIdCode("3832828");
        transport.setDecSender(sender);
        container.setTransport(transport);

        Document document = converter.convert(container);
        Assert.assertNotNull(document);
    }

    @Test
    public void testSignatureExtraction() {
        PojoMessage message = new PojoMessage();

        ContainerVer2_1ToDocumentConverterImpl converter = new ContainerVer2_1ToDocumentConverterImpl(message);
        converter.setAditUserDAO(new AditUserDAO() {
            @Override
            public AditUser getUserByID(String userCode) {
                AditUser aditUser = new AditUser();
                aditUser.setUserCode("3832828");
                return aditUser;
            }
        });
        DigiDocExtractionResult extractionResult = new DigiDocExtractionResult();
        extractionResult.setSuccess(true);

        DocumentService documentService = mock(DocumentService.class);
        Configuration configuration = new Configuration();
        configuration.setTempDir(System.getProperty("java.io.tmpdir"));
        documentService.setConfiguration(configuration);

        when(documentService.extractDigiDocContainer(anyString(), anyString())).thenReturn(extractionResult);
        when(documentService.getConfiguration()).thenReturn(configuration);
        converter.setDocumentService(documentService);

        ContainerVer2_1 container = new ContainerVer2_1();
        Transport transport = new Transport();
        DecSender sender = new DecSender();
        sender.setOrganisationCode("12312");
        sender.setPersonalIdCode("3832828");
        transport.setDecSender(sender);
        container.setTransport(transport);

        List<File> files = new ArrayList<File>();
        File file = new File();
        file.setFileSize(21766);
        file.setFileGuid(UUID.randomUUID().toString());
        file.setMimeType("application/ddoc");
        file.setFileName("testFail.ddoc");
        file.setZipBase64Content("H4sICFv+l1MCAHRlc3RmaWxlMS5kZG9jAJRax9KzyhHd8xQue0nZJBHkciiRQSCR08ZFzukn8/Se" +
                "e53TwouvPkRohp6entOnz+/+ePbdL/b8x1KPw+9/if0G/eUv8iEds3oof/9L1xF/zfzyj3+AfmfX" +
                "5ZBn/Jj+ohh/9PH6+1/yiqTwX+7Xga798p8NEL/8BTA5LL//ZbWu028R5DiO3yztb/Ic4euyBiaQ" +
                "Hdz1G/RXvwR2+XiNxbrLf8GNw5oPq3NN+e9/KeiswPMC/yf2ZQvU45e/+OmWIe7BpTVf1gL8wn4z" +
                "ZcUvf6FkYCRgzHrd5395Np6mrk7jFQwHGdM1X3+9rD/yuP/lL+z6BtefNEn9H0NUPVawavTUGvOt" +
                "pt/2oG9+Xb4F1yilzrF72I9vgz92R/K2iLBGhdd/Om9raDQlstVBaf2UEklEI9fqQvzZRs4D5oYP" +
                "mvZelZjTY+ANU8fMkbwmVxdfx0tTdc0lsIY1F+d1Y5DG2m/3x8CtlnMpsvryVedLWg4X+aPU8Qrx" +
                "efq396pUN2MJYOj4iC/FDjdcdzgUvX0cAgeXftuH7ryk5ZJaweMlnhNbttNVz3w6kZUmjyXopEXx" +
                "LtYfX6/XMlTPjevBJ8kfFPrrULukt37+Xq7R//7tuiNgP98oPeu4HN8f+3UofzlmDO6pRz45QJlU" +
                "lp8aXBCV3eq7JZO8CzhoCQOrsySvSXCr09ATvCBDY14odUfHvrwJG+V0Z7LaQaGPvnP1BhHSMT7j" +
                "YwjZk95jXX+Ej+CK4vWNbr747M7HWj6y9lhOdOHUaW9Ybnmc2nuFTllJL1ruNM41WbM0l4djf2ps" +
                "knHhR/UOPk/3PbNSZY/a5b+W3nC7aVRCkeMCvLWD1oWuSvsxWJ3atBhvwPCTIGAkILznPdzDwH/u" +
                "ezrpaH6077s8yJf+qYTXK4MzKSGYt2C3L2h/Va+2WQj2Cj7fZz8InD75zwT78YRHKmti/v3aDoIu" +
                "4JVcKZxOGKndyi/JlopTZMXGQEFCZ7MzvlAiTh+kuonvK9xESkQCefnSI8H+SHOVeL7Pnd4XCRn5" +
                "wKKMUzHeGotI+6ZCEo0l8LGguK3hBhPD2LfIKH2FySL48UqPKVbpSD5LnR4DjhN4Vnsnu4zwWIEb" +
                "mI8SIRQcsfkUv66Q3KH9+JhXMbmlAe/BY2xCIXm7/KFHq6CqI6/AaD34zl415Nnna2Ib1OeEvkL4" +
                "YmFWPl8MUs7GwfO9PB40F1q1HKAvCtN1I+RXRsIfkhqf0uWT49luLNMzToqbKWRvL/jHO6AfnI/u" +
                "1nsrr69ptZfF4y+ZZlW4iT94aUXXyMa+abKbimVCNb8+zjgS5stLoVdAmA76trClzNpkqXlGMR4J" +
                "f9RMRdqKolO682NKq0LUYA15Se80tYiH9HjYOfnOX1oKUZwvdDsjvWjW8LmvfRk/FPKsWT6JSp5T" +
                "lmoqFD3/weEBcRiNZNGukt27+rKUB1NhoQ/5LyNbXvC3ld9X+xndJJLFNkpLPsCW0xv2qhMelZiG" +
                "wxgStRCd2q6WJHuFo1luB+Lr0MHsbc+zZ7FlAu8uaDS3r3cnOKWKcDujq2GdIuMhiVYnzSm3wULQ" +
                "em7/wn/o1igzsQUV17tAt/3Aa4HR7kgSx9eXQdRP1xOPb3J+9LZUfXfyUVOJRyNVXhQrNbRxUqWC" +
                "kPtKQ19jTU7B/bom+tSaozTqCB7TK/6ygfWwsPPYi4qA2cFSlh9dWpZbRDHWTioUXMXwQFBQs7Mr" +
                "Y8mnGLmBR6BYojjJw5EfHWc7m2+SWzihLUy00a5+0iYj6+goJ7xdk1hIKLGvIP9ZtgaHboe0iV+u" +
                "yUetUtN4v56C8+OUdQfBixpFq4nkorDZ/UOgDIRfL/59L/oJixi0CzT3RS22Hcuv9hjCS5gu3qOD" +
                "NttVJDdfeWjrI5k6JEtcr6JXEAVjTFPY1RRvP9PMQx2JflGOQHHElJ98latwDf7rVZCWViC8SZfD" +
                "HtWLlbLkc6gYaXASrEm5euqhSmpdsEBefgay+GKmoZJSl5pcD9/Z3deCmzz2nWf8Uz/SdBeIcbDq" +
                "QlZwkns/7xe/4JwYj3cO1VhqEBv89GVBfmSZUNKDpihea/xoOZ3++k+V/UTc2jNtfDXIZZ4I2TCV" +
                "1VNJ0Wp6vECTFeYMesZOVZhTUGVOgKECjTtEG8iUv6MpKnTqq6MUzx1FNnzHh10IrI1zwSL6QWVB" +
                "ufh5WEglwOx7r9Vy9Bc3Re2eOpWg9geQ64WMCYizQOPk0eXKM4rVa0SUPvIa0nu6HTTv/RzFP7q5" +
                "8Wcjnvi5Kzzaq7ObURxG/U45SaXCEL75VHHSV54p5I/pQqzuxzfHUM4Fn9DV0/PFkZVHaufc8Yvn" +
                "go98NDbMpZRupQaKECiKjIYknLGt/BiV4tssVO+wQnHGUQwVbPKtBaTCO1rqk3T+5CaVd/FUegK3" +
                "suoT6a5QTWYhYITkaPta2GCeeGyqquLpaDIadNGv91Alwxv7war78Srqc1HWghNok+WRY8e0rJJt" +
                "pmrgN4kK6vvKYJF7VSjnRVZVsDK0vQQc/SAgUm55ZVjCjvKHvFGvr2/S+4sxHvFjdA1z4kh/ItXC" +
                "1Q7akXwznY1S+TFTNvRV6tJWhOSypmpAlKWwcOmh1SEnMWqDG+gJ90aVBvXXdW8qDbOUI1JWXeov" +
                "LiVj5zFQFR9yGV9cYAju0NNJ5WlWtaFfQwxiRjjoSttpTn3wa6rWnVof6Uy/tq5WVcaEd42SIfJh" +
                "K2LuyEbw6IZe2Q+6vqowqnZXlZmPyQoyg6Hm9UM5EJaIZAxlc5J7/DgNrSSXChsg48Xvh/V4mHL0" +
                "CV7Gs1VQmrywq4rfltylqDX+mHQZz37MLD09n8Uth5ueSgVTjGe2hT+gYbQqJsmPVz/WK+rB5dPP" +
                "mqtYiOzFdsyX3vQZGZVMPBop1GlJaFozrgIypdEulIf2AYW1r6IsMVVxcIjGaU4mt19jQPhcvD/k" +
                "eX9Zz7UpTZF4mAVDv+QF5qXkRVRwYiRBkl/QlEWZjiA2NXReF+g/rmXvO53KPed9b3rl+SpXkMyJ" +
                "4fJnaRuXvsh3FDpxN1F+fwslDkm7Sf2ww0l3pdWtJ6G0lDH+8SLWdrsWDPWHIhLrxxQRMCmWQf4e" +
                "Rdx3P+aDcGMf/9FH0OdD2E2U/Nh7bWq6mVIvXDXjWaI2pZ5TYbtsxMMp9/ATnBELeX0GsaGqSJXz" +
                "spwR7wWy/Ot+mNx0ZFamMX0b+LMp3RnB3zmKwR9UQXs1K0IqwNVTCI9WrUXdrO0BR1bZNiYGOkrx" +
                "k1BlPMa8H3bsVf3AYdNJEgprhWXik8cXt7sL25O4MdZPy5yue835hmZMVQYX/wMKMpCpf5SJ77XD" +
                "TJfJlT79F529FTSQq57RJIpriDiYliEjcWzqjnytjPHYnuKyTwTSAANfUWupPPhkFINZ69TwaWio" +
                "2g9+YQpNbwl6Y7L4h3/+aOSSMEpf/ZHgPJ3Wb3I/LhOHfkS8KT2JLo4CVRvMaYn8w3ODgKR1Qhc6" +
                "urnMAWeLcHtkNL7LfvS6NVS7P2BPsB7OI4W+phvxmXrY1J6pa9biDZGDkkXhyoNIzuXeruDOBOxE" +
                "w4aVBGKphUfxOn+s3pdLMFkWoNvq5niidU0SLNxG+HLiOqdsmzgJmn4LL3JatIPEbtQ8C3l8NyRH" +
                "MQrxIMeYfOIiL0KkdDE/9BLD30G1H6JfGoRePfUOBLfQ+/tDunmF6u0LE1/FSWuaaqX+UqkWlgvP" +
                "02oGiCFIMs3kXtcCgBdR+zOVsK8A88E82w3//FQbfSS5RGQcKzs9uvNB3FGvJj2dVLUpA3I+W0R2" +
                "VdrSsx276nnnzpE1q880FVKTxzyTuFMQE4+Pl0TXczQZqLPa8JxgU5VgMwxtnTdLEmWQSTxF3rjP" +
                "E0PP4VLlbpTNJKuGj6rnukuzgH/j9KB+WFN06y6i2sj84wf7hhovUFXXmjuwfmpKS1u8vhEnlSvb" +
                "GHCAFwK29Dnsu6Utsd/k7g9nnEdd8yoN0cIz+4LyoXyk0QjwHTEzmhqWp2toMYrPLiV1SXmniLDL" +
                "fIt3pDC4RNRc2xy+sqjwWOeH0xg61EiCsXcVJlYp9mgFwvicY3fOMv7Inw+3Ho/rEdH6V6YcA+1z" +
                "Ga726PEIzcbC6U/HrjvE4Ny2ta2XlKDgQpOFVJMOgIDrevzwfoTpBzaGqZsS33WnMSgW7n6UfBbj" +
                "smq+iA5TeihyNb/1SrkHACyd7HkLiD50slctraK28c+7ebFaZ8iRaVLHbc46Y3Wi5NXeN+n1Paqh" +
                "CD0zG95dahMTtPvqmFuqxeP7kU9TPATY+czGp1c2/SEYhI22j+fsJS8Pfqksydt0yUBpN6O1YZyS" +
                "V7E8XWvCD1Ot/Gi1eoldf3CHO6xq3Hq9l4td5W9ofEz0atDcsxt4fFreELD4NGDdSvwJ/soqZXCd" +
                "ujmKUWa7jgpGbyG60UeIXjAkYxiwh/HFjRXepPJLCmfVCmFS7b7JLyjOs1HiGMNCKbK71dZJ8jgQ" +
                "fDGKvDrKxO4xv2vvJwxCEjH9nJfNG26bATsTvjzTcxwv2RI+akhdT1FN6r5bzZh7gUG/M96njOTH" +
                "cxur7+I9CVfOfFCGcG2KZLbbqRCHJfNHHp9BODyNB5MA4M7QWhjxwuTGWqB0wfhM58yvqbBvzJi8" +
                "63dbwX2TzPhUC1gJwS31PuM3d2VcpdjwiXFKKwblJ5gsWF8oXTDO56BgEvP2khMP+cmLNA99b7cl" +
                "fQuw8CEdZr9h95bBMA7kx/Lox6+RF6OJ6FJ2vtWb0eoDgd1HCo8KSu3p1hzE94V9L2fQ8oyboFl3" +
                "KHktJkqYhq+hxrJ8MrXW4Xcfvdc7AXkp/sF0Zbcnue2945v1z9Kl8h9+zE9bHKeQ0Pi3tkpHMfbf" +
                "H7EhpJ5FBkK/XhXAH3F41M5bxO2GlN7+l/TeU+iuZr+WdseOvgF/3hCmvYV3iS7BFixbGBmB+t0/" +
                "0f5VDCbsffmjAvD6gx/u1DujIqLP55Pi+0bHaHqaQWFOQLu/6j/qK9KH0Hnu0t5WtYNe63A/FMwR" +
                "HnhofpN08t75g7r372Xb14+H51E+STvDW/RIiIH7FP1hosUL9vrp7SdD+rYn/6tMp3ONl0DNNOBx" +
                "8sJv6scs37wE7+qgREnW31rbYk9IJZKCAfWqiarU3QF0rtDDq8c7CSGy573dSddE8KfeVGcLpm+N" +
                "SiGdnhpd2zeeOOq6QUmg0hoJk8/sCLaLyuwnra9nc4lkusQ/njMVewmupdMnkp2jbLapG7haXqXv" +
                "Am91oN8QiQzL8OPbkovP6gd5PZiXxSLh41Ja3j9PhdboLRQa3U77yf5RMmNGne8lD5abqJrRTCDZ" +
                "RD75+x4lV0palazsCfE8w4q3+u38mB3XFUbb8Yl5HW381OL20+LJ2hZZ++QcXDVcaIQHq2l2mUEs" +
                "0mnzfIRf9glPgoO9g+VakPjFUIAI29WCfKVYAWePDdbhnT2FFOVh8wn1PNIjWns3MSwSxZV+NV4u" +
                "bf0bbqt3TnnZcM+35IGp6j3/a4Bouns6/eFSGpY7bylSoLszz9iPfHbdPt88rMFc+ukDI+egX+GY" +
                "Jzexnz7Y/nkVH3XBS4y+RYd5fh+Ep52KvLgQJ0YIoL8Igc8rdr9QRxFCjvMfWhrZ6DKF1J2MjeQL" +
                "t3Rr1ILXfhS5ndVkVWB2ZQN/oIUkuFJeAMNR15Ef25/w0371e5Pi+HKbmBm+qLhcwxsPHyAbClkY" +
                "JaGtGYI1DWTQcAO0FO6tZTStIObsCi78Xay3+XXHU+nzRbTzN2eLN5cmAQMQfxNOLH6CamqjcdYW" +
                "Exm+Id8E1ePLxnf9fT1XtZkSu/+I1ts+cEF4FYer8y9akN7qhFzKJvx4rCBHJqK6v8auetIvqJvv" +
                "YcmXLprPIaJyp4r5QC+eVNGBAi69bgNsqy37g8EEgzW0bTk2Kw736nHHicgkhA5lPwat/BKvdq+k" +
                "jG5xcL1IXWLL5gRUe+R6R92nULcdd5wyX7NjaAqrseiEJIKE+FIM9OgvWpkDJXbMVsLDkJX8r7fM" +
                "QiWfntoJrsKs3yMmPxEDC7K2+uNFKjfnkLnZq/aUhxCovnivDtnmXcQui3WV1u7Zl0g369vLAtbe" +
                "Kdd/rBRMYyVZ1ki/P1XUNrnz2EQ7TdsOQnjL+mZD1U4AEpWZljdxriI3OluDR9NkDz+Ti5CYZ4EU" +
                "NkDnIoGdJ519fzSJ2qMaDPLBha+qYUjj85mRAQLj23dv0QVkyYh5RkXsJH3XwVi6DGyyrz5GqT8w" +
                "kE1xspxp1V8laOw/X7qIcazY9kI9ns25nyzfT9+XKl2cKhPZ4sxx88GiaKJo/81M1nWRPqomAhmF" +
                "XgcZOWbDrEK5z3E9qWt/D4/7gRJ3fhu5ffcJUiH9k1a/vm34n/fudZ6UeRNhdR9xB86aob5tMxxg" +
                "EC+xUhw1k1VBpw7rN0e8uiB+Ph2kA59VtCGJs9MLFCSAGNNrTH3Xejtt7xu6FzMHCJM89J+4NCkk" +
                "K5bBZAIVEPq47mB+HAkFa73w4D6E3Bk/0gOXBDSqdW3+0GlsQm5iU7Ab4Qs23uoPXR5JAQnEQZSi" +
                "YDH9pLX8s9Uv+1Qfq3XiYwATSCUxSphZoUgPSQ/AdqWTC3xFxmOx4Rgb8tMEaMWIQN03Uj+aefrO" +
                "tTCjcz+5NuZz72kVx1w1dM/FGaqoIecWvB9E5LHDt9X7PNByHZNnxhOpFUzfzlT+JIWJz17mPQRZ" +
                "sD8qXR1wzWuUa2nXgYe46ukueuff5SgOmKH+UHm7Cb66yoGdcb7KSuthkkiYaG4GzNrjJxnrQ5Tm" +
                "8vMHcxxGACUasU/DusHwm0VcMww6UssihkA3MwiQJmJwmIHZ+3DO8c3QFL/f9R3FOAz3yOzwAQJl" +
                "mwEbcOgFe70phBofCS2yba0USQE4v6F3rovdStB0ke8uxp9WbYPyj3sEL38+Et8uoDitnGPa6+oW" +
                "fwh5yJFJ6PLii5sxHs0/qsG4aN9iEaigYcDDMd+8KQNYqwSiMh5vFhWgEKtfMyxTEptYZGfqCwwL" +
                "yHnz9vqypEWfKY6TJDHhNV4xj6M9nC9lhDb27Njo82NeU6jgcImzS9d2QEUau8epvV5q+AP9yeDU" +
                "gGdj1vrogAbc8B0Ac+8Wo1ROP7PODfgpvzjow5V50wurHw8PQUr7ceQA7pjipusl/v1DlLmNLF6E" +
                "/K5pIqXuPmd3mbMveV7Rs+E7F0L8VA8P0iNxPUyp6lv3sj5mY3GQFQ9qWHCN9lYG3NGxYAMHz6xe" +
                "cQL6W9cF7yAbHqq49OH01hmv0TXooOOE36WvGx1GgBYGUvUNgD+fBSe+KsV+xe6WmXQipFh/YCZ8" +
                "Y9IXQumG+epZb+wJIUvZ1R/M494UfCDEBaDcH49Pw9KaJzXlQq6Dmt7cZPnMdfRLWhzPK4DWSa0c" +
                "z8kPXixqIiQN0EbqdGVBKobnvLmUiDvUxvD15AQxtH6iB9vsWYamDgiJLXom0C1YH61watUV7mtN" +
                "Gph76cz0MPzzGSdokldRjNUG+lin4ge26KZSI+99m3/E49w92SCB3utJJjrN9jKjXoPc41aNVICh" +
                "EOcvICZBnn5lpZySoS75249zJQ9fOX8MoG68n0VtpRN0iUVWOZ+FlMXFZAcAbREVN57oySPx6l51" +
                "3lMkaxO0Liz23q5RdRbaa9V9BjnEUTF+QKrXcJ8MHcOsTSvu7Wr5N2d/NNL9zKnX3pr4UIhM7VVd" +
                "/eNEuEXkH4/0aGjtcKKlQsoMitZhajZvlkMGuySXJUUsJRbkmeRfMjI+7zc7gO6WrN1rGQYAa5KO" +
                "gaUwk6D2DyXzTxdiSaaR9LVI8Csroo9lCYXwOOswXHdjphRNgenWmDUQZ1oTVT0Js3Nd+ElSZ5SU" +
                "CzEFgQSEfKzPYnFPGfarouTwH51ChgSbFxuMIOGGBtEGx7uR8oVeiDTuM3lN4eWPhige5w+ImCz6" +
                "FEM7cDcTfuyBGi3ItoZbJ93V+0AOopCN9yLQdEzTgBVST7lCPU9gKg2D79rNITpP1Hikv8d10mj+" +
                "LrCM1y3Ksf3kM4B6OjP67hw5xiQ+yd7CI/dCcPblAx4drpN3FuDQs/3xQXZABrnSBJiCUXoCzpac" +
                "oi/gXp9NQmMlguVlUY/U/HCVSZ2xBPTJonMXTThFwgV6B/C5E6BJyVaheN2l1329QOMeqgPKj6si" +
                "p5x7rcxGoM52mOa8wMgPMLuP8+2v1Q8ZQJyNCYoVI+Otp7L9qQ+iu39KEyBo2hGIZ5tRhvsV20Hb" +
                "dYEtqKJ4U8TB7x4WerdN6zMJTRH9ajXH2lA0lnZLPXxKjzu81K5SfAG4W9AcIp5BvtfUVDCccbS0" +
                "C2s8xrc/nvsofSCk503s8rAEtBKNuQ2KIj7fHs+SFcgEO8Wan8yMMJ77ahEaXqxjSPwPGt4XGfSe" +
                "VGE/IeWlPvdsawsZZsnPHpbBx1hyUBE9KTK3sgQJ2b194wAOAWfcEZPOJFkIAVnspv2UsBbarPsY" +
                "n/l2qDFMjBwRE6O9f6QOPsI2EIVXYAsFt4cGG7sbgQNU/cOZnnuAOwxiOjwI5Rdx3fne4oeEvVex" +
                "lgyvVmSEbb4UmZShGxHetHCEq8IP03r4Q/UGddwjbA5YY/LTeFKQYCA/zOLaWURG/AOp47ho2Pzu" +
                "PoF+Etnu0PJ0DvMwqA1D7njyDX44YENw92jc7tOOTmgMyolZpknep4OY4SQyh+t2QOs5IXLHty6Z" +
                "oKVoL3CKyVJZz2HhwSrydok1fXzNx2ZCYF9EfZdhVMt5vW1zFpWLWuZLBhteFRN2jlmoT0uGOscB" +
                "hfcqe7QIr4Ke3fX6KCwJsrIy9oe3VNJmyiQ/mybaSu2IlkOrmqHNJ4HdluxkYMpEVtLFKstIap6r" +
                "LgNAoQt82zMkPrVKExXBNl8/mOrdTm71oy3CknYzO22WnrP51eTn90+9EULQKErEbt1IUNRbSf2Z" +
                "OJDPzDTR10RiEnsDSMA6ibUOLRJ42eqo86zl2XVuIetsJuA/riZWt9wH+S2ZOfQJJqiHpq9Q42mR" +
                "FpstAOaFMpfX+3nMk8J9LNPqnU0JWj3M78x4WqZp1q4il0YSgY2maz1pWaAfLt4Kwei3i/hGUM2w" +
                "+AC91ebrazPyHDSKr07hO44tKhdHPoEGi7OP7yi6Sd5T4J+dWGHyd9za6V0c33EIQK5Y0Y9OjKY9" +
                "sX27ogMFnFoNreegTm5KcAla1uYP0JZPS7cYQYPilVA5dvs0Kf+gLIByZiBekGpEnvFR40GiiJeW" +
                "nS9Sj7hcMgHFEXsVvPwYo8MLXRcUHChWWc4xkjqL9BygFrbku4apgPobTlRgjZ2XHTwzOvYLy2tV" +
                "YbR2Zc6Hh6e+rc7+YgqEuNXbejNVXmKcLadgf11aBZc80Ncq7cIUJza3gvET2RLVRm41573iEqEz" +
                "MZ4MaIJ1haQBj5Bma0MmAgocpKTDHc0zGq53Kl1pRPhUeyLCzT0+O38Gkgwjf4UvOpQSYTV/UC8E" +
                "kgi99a8vZg2jm+KZGIyqW9KmG9uELddmZ7jTbf3Ylstp1BYBiGtlL5GnQaB9jv38kUKGFxPL8dBc" +
                "SdP9wJ2U3qGnAvtSQ7Lt1kY3/LBvr8UKmUeSoIbQG3ryPYWT3O4MiGVOKAINS2WJ0/mZ1uFykevL" +
                "tRZQspNX/jWkXURLTJjXwNGiuvv7n83bWn5xgO72IRzJZA6IO0JkT3+MKhfDpcI5mGguAIrwjAq7" +
                "zfittNh2NU+VTYwDjOklqBxuO606CwN0eaduhfXjpDLs/UR7YZjZSiYFc9RbkrOrgFWvVmUFAESq" +
                "uNqeyYvcpaG8vWXB5SQOQMVSOBb6XHtRAMqN43M/7OZSmMYKGqNxI9tZnXDQiXjQZK7vKwQssGmU" +
                "Q6uwQtkYNwJ6eO2jtp2aB+vhgqto+FoRkIIY+yuatEw5J+3z01rv/C0rH/LywC59N+MJqHI29AW3" +
                "CwQWgZyesWrWLx/wyrkvtAv19heLRMQawUJMiFbQfuPP/HVprIb7Sgn31/1pesfh2kqDjlMn8bjE" +
                "q3HAUVD+7gQ8V6ATxcqjTJifKOeMrdE82moQbRkT9YnGmYNNP1ymIxRvF6C5/noPzgYIhBLN7LGz" +
                "jy1efa8cw2fl39PshwH/fBtfZVssQPgBeNwiC33+QNKXPZWVAfkkhSJd+K5xx4WrzTXkRIHnqVsR" +
                "P1B9AbMn8qrKFrcua8vX8sKQuRHpZnI6Z5FoL4dOcuKxflk5Ck008a7qa97kxc59/LlmM4Vri0ZS" +
                "NZp5e1oCdY7i918S78ZstacBQPAIinBB+VGXyym9NO1zTUa0Gpum1I72I7Pi+Tl3ieeubjd3qtem" +
                "c7/M8+Wp/SzK/oXPvA/NTjBbmhf/8GwtnkcpM3/QbfjVCjPPxsY+rLq9tEFdRuV2qE/xo3uXU/Ci" +
                "gQBBUj8zFkKm+gPUxQSIUVkdqOuo0KFmVv86wFq99O6DfuzLVOkfcTn/UJafOqGr6tU4PQ9gwkEz" +
                "C3qrwaT5AqfFGGjrSHUQDcsHv+TS4psf63L70YZQRGyw6Dn1+Kp/0uFuIv/ztLIKgBgYIjMmm/r2" +
                "AXjEZH2rXtE4mkKml6AP10cfirB5Xl/0gJUtjakSdnZak1BiCnuBgeO+6oEqTIT3tHBELv6xsZhC" +
                "vp9jdQY3aDIOZ5ney/FRBwvH8DPHLiY4428GOhRjpwqvbWXWD2R/WVzpRVx5fL/VDjq4DyOZhOW4" +
                "JFRCKX/61Eg7ttgGJkXlGEEjvrG4stzd5tktPBYRchJlJRSrLRU/8YVS+mAdBiRKbfRDpdHeJJ6X" +
                "bXJ37M8KgXufU8qPaQ0MJOtwmL3QQILIoaXwZ2mwn33lHQqIPwb/ztD+m5mVbQN+Zy13liRWOSKy" +
                "H0ZARQqSedeTwLeta4NuhUQMpNXCZXIEVVbeR1eFQkGfBRsRI2XOHwsx/DBwXe0iK3CKL54/RR+n" +
                "ubej+V8QzWIBcdeyjTNZUmOgewdwXioyWmDupEa9MKS+wORimsGDYAwSPZrd2gjioR9uTSjvp44M" +
                "0OnsRvRj3Jijhv27fdmgAfdDE/ZWA3uL6KU+dmueVg+98EMuSjd2yX7r8QIwisr3FE0JEtl95ZiX" +
                "J7Nj+uq+gB+gqc+j3MWXL/v6uyH44JHZiPtJqy1BsFJ4s6rW3RZQ5izBE6WgsXtXq4qudky9QVfb" +
                "lpa3FWIAIrGWr8dC6bDj5Tu+o9qOwBvIiZSIiDczzqEaejlyDd11xEqGqaRD5HCM34xAdJfSSwOL" +
                "1NuwHpKRNC8zByoHRCDDXqIDTUSy1ydJyYszbwFo81D3wNpIfkw8mwtjH1xn9GPpdTYQOZiDM+Vt" +
                "6X8TM/6nljEsob+JGT93eHxf/3wx/ReRpyd3RwTQcYKTKNAt3iGhTqls7UDk+ZeTTi+u4AbOdEXW" +
                "dEG/K1Cn0D8/3msEwsezigDW+Px8/NwysDMlRJVoTogDAgLUobeCKrxyAP4V1Iiv49Ohux1YVSJ1" +
                "jQlklInkljp42Aw+YCWT4N72+JjAGBgJFOLelnEgdzjCW0M/VSpUXexnIzh3AoMYVz8dILsEXO7r" +
                "of99BNEEjF4K7/7sA5trHvA/izm/3H+KOX/Scuqgt/I3gevf9K3Q/xS4ikCU6tKCwtoMcwb7xj6A" +
                "4CUZJxtocu5kOjyseZr6CO2g6ORg25P3H8SrQ4HgCqi3/CivlswgAmNIvvZOyj5QqSQ/+K9ce09F" +
                "eaG0v8SjFBI+Ay2B9sM3A8mlk6Ibq5tEk2IdL0T1cXXV5eoqSpY+QVZppSsBwO+kOmMXnzysK1Fg" +
                "Oiw0vWw+JH/c46KKd1pp1FpH6md7yJ7R3ggHs6FNMdK8jz8IVrXrz/S5hB+sx5nDAtuP5Ashn29s" +
                "OJIOd4A97hL1nVq8an03jiAXvafxJ7JcDzj5oohKFNjMBWNnH/TbcWs8WEoSgbAzYq53d+2lnHGD" +
                "Yv3vsG3/a2RCP0Whhn2wcLDIVHLBBRWLvL8cm714R260J4O5m63I/hSlb1S8Yl9cHM98/zSlUAqm" +
                "zsSrKgV6XRA0ehj8/fcNfgexb6Gx/Ek+t3uAwMGArhf7KWI/josrIvqGgBXwBkuIQJimfXdk0hNE" +
                "2M8LyQYG3ATzttj/7BEI558DTFTeRvN4g+BqE1ydIa4cT/1fI0/S7RepgLe45QhUwf/sB+H86fxP" +
                "D3LNwfz0cgh8308L6G9vNNP+2bi4h/rX07SEcPck75GJ6L+8Fbz074agny1hf3GaK4kD8CwgVtXu" +
                "J8Ofvwx5d3vvTgjvAgvvVvi/jMKtn5/ItyYodNU9F5dD+dkxLabwJaqD5WwR6p4Fr58+yQEvuEKf" +
                "vFNJvMAomtx+8u71BH4DswCs2haq7LbNoungdWCou4mDJSuBZS4DZuMvaup/84X595iA/hYUPw/d" +
                "z7r0GP9jyH916O4AvQ2IgZ+z0U/ngPMx6OuA6eHN66c51dB1isCcg4d+dupfzn327OcsJPz7jFw/" +
                "GYF+9uo/HAmGL6IgF+7R9dcRXS/07yMg2C5pxSb7cy9X1qyqEYTf+RWp5NFK2EQllaQyrIKCsgsv" +
                "KWTfEVSQX5/Rs1zvyb7WLa/QM/NNTy/T3YMcsbwey358AFgZuDpC6UsihR/FITnWdn+E0j9ocIAo" +
                "XLxbWoYsLuhG8QtVKjcwIE8GxIQHvTmS0kovbFHP4ebKJXCjde+mS+gmvM81eB/c7ve7+30hDYjy" +
                "shvj8DPo5kuHTwP4GTQu7u6ZdxPf856gFy7sY5GwD6nkkAM1L+6DsPsg9T5okmavFlmEjloG+Sjq" +
                "lifCduzB1QQ+4wpRefdOIB4AsMMLoDu9ASqFBe898W1G6HLDK9eTMkGAlw7KtIdS1ki5dA96C/cI" +
                "Hn6XG15bwEGjCsEUThuViceUSetg/xoC1Bu4kyPP8SBaw1QmaQaoc4zN76y+fxI1t3BlshIvA5s3" +
                "+o5z54hivHZimYsEmqdBPCW9XkviPHkCvEEhJm/AyPEzxOSmsr8BmEv4rwEiUAOUYibviM+A6lRM" +
                "u1/ncK6YEindAX7JYoJBYf0q4I6zqI8cIm+Iai4Rv77mhFJy5dcBTXBDlM8QWwwm3+1RtG8vDqa2" +
                "UQUFZmrQxenrPeS/7g/QS8uLV60SRDGVt/1Q1lkGuvIw6LwgKjCJgTqfFKgqDeNxnecpbXJxFV4r" +
                "hUupmCBq0/y+oTC8ZrkTtLBJ5RRoaSmnmTavmSUHdU9qpnXT84LULI/bZ0wIJ+KPhMr5op37pIoj" +
                "RwNc4WyEWti8zkF0EzoTL910LpjDxGtUMZtXLQXTppCBFvlw6YDU06DW59DJKrgrFzd1UjayYQsm" +
                "5m6Q79C314de31Dyz5cuerwTZGC/9o7PQH7TdAlKYBiGYjQKO4R9lnz15fsLTlIdN39tHOvXTZ0F" +
                "fplNj3eOlOicNuEXoEyaLjun1a/hmPodCkd1nv0aYn0d4PP66zsFI3HqDor+OurzKv/MPB/57Xr/" +
                "6z718ccUH5AgSY/iqIvqIPrC0qXvv/yKw+4duSyJ+vPfme59qmeId0TbLy/RDyCqGq8cRe22EFbT" +
                "9RqNy5LyKklBm+/hwKeeEOedwc+YNW9t9M7Opcu+ic599mAIw2mMvL/KBf999aLgfde0UXfOov7L" +
                "11Ua2Ncfm/77ZVPrlJoFPky9Rf4QijCbynPWcS5ZLn2P/O660U+G+sMna7h3fTV8uBxJ/PIHES+y" +
                "WegsbGdYJu6kHEkhT9IDTbnXrthOej46yd7LVS7b66vpuNr7prSLdNKo/VnelhukZvlMszV4DMBi" +
                "4TbKT4v4slvwhX8aFT+Q/BUbCFSzPHGGn1pylqapfotp4SQnAm7F0gyhpXixrk5L9GZuNpJtrTpV" +
                "WKdOepS9sCVDk44SUd7e9uVMXnPbuC/mzsqeZ2jqFBdirq0QoTmV0ZawzxJuXiexMoqFWxebHckU" +
                "01Sq86rjF/RlNWNDLWbokDbzbO6u0hNnLY1+Hl6RzW5GoFuNl7fZWVAul1V16LLcnLYLipJATM4A" +
                "Uy9Qs9wd9aMdq2uf3865ZZXa89SRh3aBiKh3zHx4ckG65GF53UZ7Fk+S76Flfi55qJdNdHvo5HH1" +
                "RtQN8HSnNOGlvPQ/9GRf4VVz5oYxsYHB1IGQhENRVXtO2xUHSYPCAhfZGn1bdyTjMlqBapVXC020" +
                "6OJpGBRMf4lKwZ5jliyPC3ZBLAxzIVEFc9vOlCV27g64ulhMZagKq5FcbAVSTsJYuFIDmHxlhRBq" +
                "vamBOYvM4UQOTGzxsUlmW6Jd4GgV3JIDdVzmBT0HDmD0dsfNlwDb7ALVDnAyz6QEmXeKrRRexoWW" +
                "YeJ9fz77yZq02GM/bH0rWBndnqSGzt747mF3ccdKTuO4kw+ySCyNju+RuvDSQmPa6Bye3ZlC8j0V" +
                "DvTalfDBW3clKfJjGp/UvVadSWeOBbeQdEiZ3o2xDQissJCudPi1ximHwyw2pJUXLAgXDHfVvIoZ" +
                "Cpwf26aO6vMPu3niHR6N7yToSZ9rB326PlAYfQ8tPzyu2PuuEMPd+AzbFEnim5xlwfyUwBDLgETS" +
                "1hrWBFebFVrB9JX51mtpRpyAyiTFKS0ykR4wBmiWADimQBStH1jN5WxNE/lBFnSTz2B0FwFu8Wwy" +
                "eJqtJBZh32AqXPkHffIc++Y7eBsQ5xJ+cCSY+IPCWI8BYBh2uq1aulXyW1vdPo4PTH4UOeA8z85q" +
                "vMMemM63mAnxb1TpmSASBmxUH9mtOyqmBfPJ0r/TduadZt3gh7rTFEl0n7lGXtneKwB7Y1t840LR" +
                "m0EEj45bjhEK3wnbUBR6aa22HkGl4dq+IZ7J5wrLvy3Bt5QdbxUUs+VTwSx0w7D6XpkSEhZxE+Tu" +
                "sSTuIVDVhpJUEKbkuFSQd5rJawqYP4A2ybA2eHunC7JsTLylMNKDzujK9gPYgCQJ5ACyzxon0ZCO" +
                "JKfxENgCYC4xPAADbN+CRoI0dopuR8fPsD0nMhbEDAeHIRHW8N2VChZ6kILU1th5aQqtLbjpZino" +
                "A244tDljmAVVcpqT3aiFZR9yXSvqdbPtjq28R5q80tbSdT5dDeIcW2TX7WwMO0zruOkPQcXgKwet" +
                "Aac2E8yZglPA7Clqz3YxEGARS+8jRLr6dXeYedOGicvrZWeSCeMCQSzm+7zntIYacHs6RDsp47J+" +
                "hMlZJam4Lu+d243ATyBGQk2+TODc6tO46dG4Zs9W26bBgspFtxLoWV8GxRGeDffGLdIrUQdiucAJ" +
                "PJ9PgTGVJIdgYZIfmCK6HALXFIVZA33FJ8Njrk8y/ApqbCnQFnc0dONiu1oIlDGMKVrKx3LqvIRD" +
                "QKItM6aMGyh1kYNOpWsvVqZjJgOkAXBgd9f5WlsxIF5BFcEUtgCD9dInQRi+GQyOEZmk75iE50Ew" +
                "j4HGw+IHkHf9s2wv3j2PGSRWAa7PiNT1WFmDpLnSZnAZhNGsNUh4R3BSLFyDxRae7oZkcAmI/uI5" +
                "1tUlmWk7gfCFizkvJJpVHNYY6MWuTagD0ifeXPfD46pB2zUWDFLygvwA1hReyIGlgARyqIG8cBlg" +
                "AolJXB5IFORMG5D18FhLzjDJIDTAiuBTWNtmgotaLlgpZLrrKdtM+NEm3UEDj74xwxX3UhFvuB3M" +
                "7F336q91LOCa6xYyH96o6eHiBn3zDsw1IErsSMrUllBvx7Vy9eCBjueUxb3yUwwqR4JqGDjNlTeN" +
                "J6XXQIUCFBgNcEkCBS7vrZwQo04jpHw90v2wOqAdq+nOYqc1/dIashEh3DLGOkstrzdivdc1f00J" +
                "+4s0ygY1zsSxHGvqvOuMTl4d8lrdJgKmdFd9Q+K1BNSFZyGEc95Q1GyfjaUFsxEAfXKmXeiVHB54" +
                "+tRGAiXXpWhXtkADOY3JLWDMU9+6zZpq98eWQuKSCmZNdNYv9hxNjUjJ9O2alKXSnUXefpeP/mG7" +
                "QamdSrmYL8HafqdQo6JHgpcE4cE7IYGNxabcndBVTxHNsVqa9ineW6RahCUNmK0sRptkdGt/q/C9" +
                "NfNa0yekaoee7bakzpWGYMedSWD2ehNfr1qUrawF2anBwepz1GE25oLukiur2wRx27ahWnfOzoXh" +
                "6GN4gZT30IN+Sil2xzwKzj98p138MotvWZ18SlZfypU/kQB/+YXpd0l0fuS8n0qev4X0mml+RPiE" +
                "+p4ffWp8bYPMm1kV/UBg+PxrbPE1jpvY4luK/pZYet+hTz0+9X+SECTe716/XvLk/zxbv4ZHE7tF" +
                "m4GO5ZnuE05BpDBjveDcrxQpn/El9TBT64yoy/zyNa94Ial+Ff2VQpP9nufR3ffA+MJ4Eca5jyBq" +
                "lfVF1BeXHmXV73nD5CXua2PzBZQtjkaVn5UgDLuo779vi+zHx1+4+A79nIlXrl5YVC/VMer+Cl/k" +
                "aoFTNEngc5wgoI2sVnOawpaL+QJfrZZzeL9Y4Tic9MMcd0l9JpwXwcGvX9X5J3Nqyiy4SWFU31sh" +
                "zi/bqrbMohA2oH++5RMebPsDA47Cu3++uORnjeifaX2ifWfV/Ucacif+JgPfsQ1cQ3SOnqSjRzFs" +
                "uBM+XcH/P1nhP3UO9IM3hGaZ7gFa1xRT5znj9ROxO6LawLXD/+oNhvn92u/yC7r93vTLMqvr33WQ" +
                "nfX9jjX2d0eBHnK//ELnjf1O5Xj9f3QYnCLIOYURc4xe4dhyTlJzeI8vCGJOkzS9XJIYRqzoP+Ex" +
                "yLvLfFI++psG8tagR9fm5a/j3OkQ5C6Jzy9frz45xetJiQqjBjx36dumDqNO4v5nNTzNDDmEXhFe" +
                "gigE519GEvIeSZ46QEl9vqB3i4beAOrwYa3/QhT53SDiSwd3nV3n1eysWwtNOJ44D2vT1AikX7rN" +
                "L7iD2J+0gz7pDP1NxT6ZwAPiURHXgd/2lxLSws9zj/fTo3dt/MTyuvnlD/eC93rPzRfGe8EbtDe7" +
                "CruLbo9qMkmkiqqeZv5qwYv/KwVvo7DuW7UY65aVaG8DHDV9PDTKGM5zKCyorOTlDJzhNJN3FcYV" +
                "kaciTwa6KwfiudXWaret7FJh5gfO5EeFU8Ydxw9KzmMK3kCahL3RkFfikHjYyE5AfqlDXRMUtqBo" +
                "84F7KXglfrAl9yCfwqeylBmVNWKLQn8Uy8sxf1p7qvzh2uHSVQXWGshjwKjwJqaaFsfHCoO9yGNU" +
                "HAvrkwedZQxYhmsmRvG6LSWPBP9PyUCSGCn/qD5eAAA+n9VW4N6OsMmGhUSQpVfxlip2l+jRurxs" +
                "yhqf/AVMbnm0Kg2dr5xcFtotXZbXC295zV6Qbq0LVkhCXXA7TorIoRLuRFA52J3mwu0QcaLkz1q5" +
                "JlKsIZyA8KO2tUW4D8RiheZglefRTekGZL2KAUzRbW0Z0AdiNAaZD1J0T4QrNla8M+UKi+S0Pe+v" +
                "In6c1YvmzHiAGnJJiGU0q2UGUazraW1M5Gy/jccdgZqkcyvQYKXQa0k5mVF5umWKqCdOlGYWvuu3" +
                "tN9VVxL+uGvd40O1QK5rf4zb2GFoWGi3UUP77inds6N9RqeGUDVmcR1vnoGl/a47bJfGqJvNTvZP" +
                "fX0tjzEWId1+L5z1tXU9NrnrJuUsnsoJk88LTW7U+9NiDTD3Ynht3ovhYuBfisOSgXbPJq6EvJeW" +
                "QzGsX6rgHcO4vLBxUkMUzhS5VCuZwmq2Wl7dLgqS0VMkkX3UrwbgkWhiKGUdiGzW3+tPZkpXPNCk" +
                "wTfBjEmSexUtMFrAghzc1jkA1sBsAQtACJgWiBhAPA0kYK3cCemdEDwa5sAb7nQAjgPTA7EA7sBQ" +
                "gJ0P8vBUECe8g4hQz+GamXbZ6hqSIbmtX6xwSwgn96CXxwNTHjkQv7Cs8PezrITRjFXZzBalGyDX" +
                "FSZ2+/UoB1Vr0JpVmYB+dNZXPHxGCt12kygbcEmzsQnX+vBxImRLymUg0hP0uGtQF1eXlPvgRpee" +
                "c//FgppuK1gpc7/Yzl79gQfIQPSb4Wquz6545lbyQFeqdcnRyskYg+GZyCW2qbOyF+ec2YN9S/aL" +
                "zNmlmHC0dWJr3TrkpA+JScY3JxP7jKvQRSCdlJl0LLctuprOsa7lp0vqLA4sRxsbjNhptGeEzBBU" +
                "DQCtt0XEg8aS8yFMKhtNt6apjju15gDLbCbamnE32p975vzcqsVxvcpPKKPU0UqYjlf9jB/z9oAw" +
                "+2WSbw+zKRxXaNpizml+5mYxrdoHbHataD/lN5dpnGvTvjaYZZo6Cr2rt565rBtd82fIximoYBZR" +
                "dqNTOnFeKrldrthwRpTlmBnjSTGOFoUSJDOBTaLOVC3soD9OrbcsYx9ctojgS+x+dgTV4uIQstUp" +
                "C/+4exzS/naUes0pfxnYPkW/V9IjhXm7fgZ8pz8CHsxpHme6bKg1DNhAr6vmQ5LIJ6j7N18YoM55" +
                "SDve+9kT7GPO0sQH7+EA+avx4GM4QP5qPPgYDpC/Gg8+hgPkOR6IYHVTOB57ORi+/9CgmFS8gXsR" +
                "Eyl8cY/mwmaYcyBtoE9oVn1bK0hOsRJgT52z0XPoTI5kFzBfhidm6/NpVdraYZt7UZKtNX+5aLKe" +
                "dvCQACkIOSO90ZbkIIUNzzqTc1Vj/DEBUILT/Sxbu//QYoSc4DtTIZ0TXyusNUiDK79tKgrg2fsW" +
                "iAj7sNc7jtRFgVov7aSP/FlAH7XNcrn/eNbLvJ71wg3Xzuh+ahjuiGS7SuRLfOwkW6tQ6zw3eTRP" +
                "toYQGfHxOo+VSdd6g6yzFkfhXscsoqJOmNww3Zae7xgNSaPihiZhuZxOVGMwk7zbDGdg01y7J5Na" +
                "DGb7PT1JYXymmJNPsVoUNqyOl3uvOcRXjiwRtKVKaxG4kynFsqaxxroOt0MvM/tksTeUqyU6M+VK" +
                "jy3eK9sS7s+Eq95cusFnao3Vjo1kV9Y6SqDRpMbdNdfFcpqws9RQgavg57j1V2djJ9/4VkkX3BTb" +
                "+zWXo7MZvhEGeebFloHIpwXvYIcNLqrFqFlNeBWO9eSqKeWtHNTj4SZzhEfxwg14ZQe29IKWLsIe" +
                "FbfbPXFiYw+R/VV3ckcvWRsGdNaPrvee4j77J/pL90V/rw6Gw39ZO8Mxv3Y+Bskv1fhndf9Tud4E" +
                "P/wMv1bKngZVAAA=");
        files.add(file);
        container.setFile(files);

        Document document = converter.convert(container);
        Assert.assertNotNull(document);
        //because of the stubbed DigiDocExtractionResult
        Assert.assertEquals(0, document.getSignatures().size());
        Assert.assertEquals(0, document.getDocumentFiles().size());
    }
}
