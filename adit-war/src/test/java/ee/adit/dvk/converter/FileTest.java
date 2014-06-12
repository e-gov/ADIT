package ee.adit.dvk.converter;

import dvk.api.container.v2_1.File;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.test.service.StubAditUserDAOForOrg;
import ee.adit.util.Configuration;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Hendrik Pärna
 * @since 20.05.14
 */
public class FileTest {

    @Test
    public void testFileCreation() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());
        Configuration configuration = new Configuration();
        configuration.setTempDir(System.getProperty("java.io.tmpdir"));
        converter.setConfiguration(configuration);

        Document document = DocumentTestingUtil.createTestDocument();
        DocumentFile documentFile = new DocumentFile();
        documentFile.setContentType("application/vnd.oasis.opendocument.spreadsheet");
        documentFile.setFileSizeBytes(8192L);
        documentFile.setFileName("testFile.ods");
        documentFile.setGuid(UUID.randomUUID().toString());

        byte[] bytes = IOUtils.toByteArray(FileTest.class.getResourceAsStream("testFile.ods"));
        Blob blob = new SerialBlob(bytes);
        documentFile.setFileData(blob);

        Set<DocumentFile> documentFiles = new HashSet<DocumentFile>();
        documentFiles.add(documentFile);
        document.setDocumentFiles(documentFiles);

        List<File> files = converter.createFiles(document);
        Assert.assertNotNull(files);
        Assert.assertNotNull(files.get(0));
        File file = files.get(0);
        Assert.assertEquals(documentFile.getContentType(), file.getMimeType());
        Assert.assertTrue(documentFile.getFileSizeBytes().intValue() == file.getFileSize());
        Assert.assertEquals(documentFile.getFileName(), file.getFileName());
        Assert.assertEquals(documentFile.getGuid(), file.getFileGuid());
        Assert.assertEquals("H4sIAAAAAAAAAI1ZCTyU3dseS5ItClH2yBJmjH0bpciWyBLZGmMwGTPMjDVL2UJF\n" +
                "ZGmxhbLvOzHWIqFkF1Oy72vI9qF6X/qr7338zvycc+77Otd5zpl7znVuLXUSUnoA\n" +
                "gBwAQEnmX/RFSgcKAwCAnbLdBLBF2MJxLnZwqJ0dEgGD4hBoFNARZSGMhmIRWGG0\n" +
                "HRxlgYY52MJROGGsHQYOtcBaw+E4rX2YgD3Pye1yAY2yRFg5YHbRsGAgFgfFOWDN\n" +
                "oRjgDz9ycvL/8eM9wA8Kg8GR8O0qGgOEOWAwOyScbZEkAC31wzvcAcQ/nf9Mh+kA\n" +
                "WEskGoqD/yLzXydhh7ZzsNt+Dw5/8WM5yA+DtsLAsXum/19HxKHRSDsoCo78i99B\n" +
                "89sh+ffRDvLaGe3vXuwHeCFsoVZwLFARgbOF2mH/sr6U2wWGRuF+ruGQQVVQrwRt\n" +
                "+cw1JuH3p5oKosUM2/vjzmkvZt/qIbUOFhMP9deKP3e14/uMLDeP0FB9UCD3rYa6\n" +
                "O1JoPzTbXdFPkxC5SIw9qa1P6rfGpXHHktuBabq6qx6rY1/NBjfmazaamjThTz4/\n" +
                "TAyU5NGNsNMzXHe6Mx2u6uWAR+LC+Sx9VAX0vVQFr+hS682E147Wohn178s3dE+z\n" +
                "sdwyci7ERrM6DjbSLDh9+9wl254ePTGZnXxzMialpLlFOVTj+Ti8KV0KNieotOSC\n" +
                "F0zPOK7WW2hS0BveKm1C6OvTRrOSLxBdk7zuqvuWX8XuGXOpaQYiWKgqt2SdReKl\n" +
                "rvgQ0SIS8RhuFnUzo4qD9KNDuDRQa1I0NQsGKSsNWj30xKVqcVJm4bN9CvLj6ovc\n" +
                "yzBCaPdA9WpFUrD1okLwUaDDi8aSW9KTKgKZSvKyTbeDNsKvkuiN0AqXJZWVlLtf\n" +
                "qHHyQWVVGwkJjpI3Z1VLP3LTMg7XiedEXaY7/3lUzaP4oiY5tgp6wdsF8GzRfNST\n" +
                "jT3GmYjnFtG9LO+A5Pvw9nvgHtbqOg3CVeqCVkfaM88IbnP5EiRF1RHzmAm7Dran\n" +
                "E2LRPlzRQnUSXpGPWQ7l4++7Tiy5FiewidFnAroJoUS9L0haxK2/gKGtDMT81IF+\n" +
                "dc/HuedpCrpclL2pdb4xc+vnvS3+OCmj/oYaDj4LZLc3uuXFSSGhJOd5WkgPUfgB\n" +
                "UVgdPLTRFM8e+OUY00khuBSb6muph/TFYtaGk4p3Twnem50K7QVuvfd63trl5nqO\n" +
                "f3AMLu/owBh6wffmZzgoA2Tpk+w71CUcROUeXbiYVqYgZpyInPOpFoToEb9R0LBb\n" +
                "DW9/uw65Q+03IXjlteLqAzF2w/ArqWJMHmJEVQVggbx7nt0EDuGbOL9bQu6QXMWS\n" +
                "woVDaVWzTaSOi+Z3ll+0nEz71DdQ+ybQhrAVxIctYPs2Wf/d7/ZpAwn4EKr7M+sp\n" +
                "lQedbPkMNxifqNGc7LLCAF51MLWJjCY4zuononxQ+s8I/XO5H0OV0zNz2yINbrZr\n" +
                "atW2PzYDldQRXzS9YYQ3KXcpUzrZmSJJls1gqA1WNUennVrBMxtIRJ/HGwysUYql\n" +
                "GNJwO68fYbZJjFYBqgnJ+etAbzZosuYJx5/6tPKJh0fCf+PqTfOjHxPvhj7li6p4\n" +
                "SSMg70Ko+DpwIfjFS0D2HTOOgZeep93NeMnSe/Ejo7VtDxP0jkdxr2jivIlG3F8n\n" +
                "jg1O1qwQmoH+c4OOiodqniZd4oLWYDLsWZaI3F9pV2ZieM1X1N/bjhCaWNs4vp58\n" +
                "fIgloJctxA/LyZLIbR+2WTla22EAOb5WmkvzWTSeyfVxnIbEuckU5Vc5j6H9ZXHB\n" +
                "a0apFZKpPqxTkFK7Rbl+p6438iBTWCzx4ie31xPJGL4EZwdlqK3p82zTFOTocht8\n" +
                "LW8cnF9rH3sq4oIsC6RlS2d4U2En5hcxKgSCSQGADrpfMf+gUEOxXbA4FyQcuxNp\n" +
                "+gyN0L0S9B4zWbT0cdZ3GkKjLe71dVVlO4zeLuQr/QYNvn+x4SK/Nge/7HQWgZMf\n" +
                "HC8oPQ7v66G3vjkve5dQMv++vKLkLCK7xphWP6NmVfAIifELwdN6OTQE/NrpUgq0\n" +
                "RDCbSZ4cL78pXNBWzbhBpZ6kjgswppb+blgG6VxgrkKbk6oVUKf28qz+17cUGaWj\n" +
                "4ZDGdGu4Tp148VmhuegogfTsGMHSaI9cMqcXokHfdHwZGKN6GFq6db/Ink+IUuI/\n" +
                "a8OtGlzHytN+16E4PNe0bs4DHzvVYnX/xJxbifFr1046TdZMlFR7suADkbpxEF2L\n" +
                "1UKXubrgB10Pit4X2+FGaPiRiTe3TODNphwA+eO0Y9in8Zn3I83KzDGOU4F0kklG\n" +
                "tbwjT8jWUjAxl6+EL6SfNeIqYQ2Y0VunkFgyyzLtH2EAJsW8yI5Ju6o53mB5Enct\n" +
                "RmQ4WYBIZ/LpNJeqsPlCiXd/rM+MdMhjp89jqs6B1uNyzW85k4hQbCRc1bLh84xN\n" +
                "4R8SngJhQTavX5wOvYJ2/Bzte7Y9Q3DMOSXVhFLIOYJa8U6zCxWcUun4FHl/rEv5\n" +
                "ZP5WZA6Z7/leb9kH7FLjp6+FJqgl51urZ+mROQY8KZS86lqgnHCo9DWXHw0PLGKI\n" +
                "MmZIdIUWxXboW2s9szGTtJp92Jcwiq4jmZfLy6Ir3fRjjO/iMV6VR7UCGbWOuRab\n" +
                "zQSmRidXMgyTXr0wGUDLFn8G7DvQzflQL+LN0ZjNXFVVPy98JWXVWmClif/hYjll\n" +
                "8eMtZNmWX8UavR8Wrys+zdLSo3JtsF3vJVV5++RGCdWmMhDEvKR9dDljfYHG1jCA\n" +
                "zKDkAfCMHWvwmeLL7qJnHOotpS5R247w3jrXqNFpGWaemvYmoM0/wSvB9VQ7i1qw\n" +
                "OrF3ZDfxkfe143ynFePVmm5o3Y9EIFgsxJNTRgwM8r4JCFudpej+GEfckd1GyW8j\n" +
                "0HCnb2EEagbycrydmhP6wSCtRVEBNaWZ1oVa//bdoLzUn79i4s27MdyaUWvneOGm\n" +
                "W98ZdTlaQDnUL9zRco4YFrUgg6j5QuT9TUc11V/32AmOZyFeKkQi2ZdokqfZnkhE\n" +
                "1eS7M8ngYoxfqabrySo/P5WmqjdPFhtPckRF7WLCU0UZYgbrCd6Anj6vvJcdF/AQ\n" +
                "zm9BWqt5JZKXPCenCb32fGjmApkN49wuTYgtxC4pP6lmIXYm6crsDedUChxGhww2\n" +
                "bhjDOytq4SCGWgqecJ7IOdaemZ56/7HLjbvW3rFy7sb1VjaRccmZiFM+9eqfV56f\n" +
                "O7n6dXYDNS4W1XGafdSusIGFYAhjTSpXKu+YA40VGUQNsxRHTLN9CEHPkDCnpRTO\n" +
                "wLnoeFLaU6ouh0NpgBCkiJd26INz37h1jlK2qUh4WU9cYE/dYpWhuaURKPTu3UNw\n" +
                "90gJYs7QY94kPemzR4/Lx6q5MSDoqhnarZxPuuoO5fXV4/G6uiE3qK+dwGlOTxLN\n" +
                "JlequDCWXjhU8Y6OoQtfehjo6V9VPuDw2AskF6ppuirsUH9JnHvduNFOMLpC2ceI\n" +
                "vUWhsUHinE70uycu85e08t4+hIM1OptsUAJ5sKcCREv9qgkJdzL0vcmUDneoeK5n\n" +
                "NLqgNbCVsMBxVsozSjz3lScezqv0f5/r8Wrqo3uODxElE5lW0u0T7JVQa5iMbJSm\n" +
                "8nBlnSMu5Gtu98PXGxXjXRsTdWeGap+xfaVDoWY7ojbPNoOWg2inj+dNRNNevjc7\n" +
                "cPdBdk8JXE6H0MqTNzguIETifo3UKmDzg/9J+PCzG7ffZaUzqSkkp7x4O7lhF3Fm\n" +
                "PNg+Ii/N27shudLexsGtZ3JGaKni+BXNtCOdXIdl4m0jeI5p6hTpFE6ofUlRzTRS\n" +
                "Op3RxtR2MgWfmyU5XNRljqyVkJSU7LT3zDFJORxqHceoVzvFcZ5UY/pulWxXcXPS\n" +
                "grJht4cW2RMiJ9EiUhkb7ygZmY0wj9uPwmekbimS9xcSTGP8K83liB0N+NtY01a6\n" +
                "J9cT35NeodI8plpGu7CaMsJjNfzhOYTZJNobeQNUmyDKpRFDQz+2TPWqM3RVKcxY\n" +
                "mZKv4hD92JBGRn46vd2Czg1qgE3o0Dim5Lqx9OnvDZ2SX2LGt7iFvIsp27JqmjDd\n" +
                "h54WoB9WZE4sXr9nNLNR9HWYfeeXxmh9aw1MBgAUsfyuLrYQY97TJADATtkVUHAc\n" +
                "dOd3Rk5h+4PDEY7Bbp+M5blEhEFcHHAUDG2BQFnJc+npKgtJcSlAKCjk0JaWCBhc\n" +
                "5pesEtoB4Nj2RWFlfnTJczlgUDK7AkwGBbWFY2VwMJm9Skxmr7XM7kg/WpyRCJSN\n" +
                "PJc1DmcnAwQ6OTkJO4kKozFWQBFpaWngbu8vUwvYP3Z2DhjkrpUFDLitt3ZGwAJF\n" +
                "hEWAv2x3GP5XUju2eymh0eh/Btox/0F6dzgwCCQG/FH/ZW2FsbBAHjSBbVtR4DZD\n" +
                "KA4q5IiAO53m4vg5/T1vHMzFsQsgg8NAUVhLNMZ2V6b8g7dNFftD2ArtUNkF/oGy\n" +
                "PV0w0BmLxAHRFpZgjIWl8HaFC0LB8Wu1dqYFkdudHAKFwCGgSCHYthreVqYQle13\n" +
                "gEHYcGjVJGJQUDnggUY/XHdr24yEtucBh4BBImJCIHEhMEgXJC0jLiUjKi0sBfoJ\n" +
                "sN9UzgImc5CPtAxYSlhUXA74q3/H8E/E9nT9oAO32CaJshKy+KnnIFq6YlI6Pwn8\n" +
                "T+d+H5gLbPuABRH5zfpn8w/bf7b4zgUAAotDwDh223FQcyRcCIZ2QOHkuUS5fjRu\n" +
                "S33k721o85twGO5XK4gL+BPYCo76cS0AubK9jlf+3VKiwqLc1xAoUTDH/g6zbRW+\n" +
                "AwUUFQXZgkHcig4IpIWQtLiE5E/+/yLKAfcuOcU/1X3fV8ifT5872lrX2sHWHAVF\n" +
                "ILFA3K9/he1QVhNUs9iRodAB8xs3Pi8t2h0hbl1jYmJilnp8O/2l74d2KipBkRjh\n" +
                "IE4GbL+p5OKjIAtr51FngdkQ/8eu3tm3wzoyQsGaZmk6PGeI8hb7/eBsWsuz6uQW\n" +
                "fFvOjXdUh8bxqiFQvI2RrQJ7mVjZPFa8ZQ2RFAtex+eVbUo2Bi/ppZnFGHpIf5h0\n" +
                "LDDRjo4O12vf0hvzHXK8NfOOsIZo8PzQydMinDV1wlN+ebCdSzRtLTd3i+dt89CU\n" +
                "J/vMCN7U2Hh9tukZAeIh57pS/b2uVfuE49ZmCmTVoupuaH5DGtozanPFfoB6LRpT\n" +
                "6C4zJ2REPWGDVqrzqO/cXM8eXBtLJFwx3ZLqY9w0kHRF2K8O+7v1ysor4NfVupnz\n" +
                "CFurM28uPdoM09efSd8ML/5o6We2uTCRlqS8IZiek4t3MJ+aXF1unmlxdey9UYTL\n" +
                "+v6F+nqK72ur9eYCk1fWoBH8O80xtP3Rxg6wUkdDVnje2a2oGc/y5rAkecjGbKPN\n" +
                "uV63hEiPzeWB5aT7+JRldFNgQJ9NTprjm033wqnJNHWr/hkCFnEro6G8vnx5uK5h\n" +
                "QoOwnje4lletMMI73x8xkdVYELxhsjAx9YVwVGmkc9jskzzBlJaJ7/BM360Q/ZiN\n" +
                "73NmI4GsnhV9ppt9mVvunv2yDXjGuy3RsstTiR1eCWkxvD397jHFNdNmqiZjQewK\n" +
                "GpCMpye21hJfzW/VzNkMr7WYQETKe9wXZgqmvnkQPkp4uve5Q2bOLQ+r1fRKy26J\n" +
                "N4zor5fNLKc/XOrbcFlAV7isQJlB71KvbV1ZX3mX11fzdntDcVcE8LCqR+lZhJtS\n" +
                "3Kd4QBHCmW+t8HUMxp4PLBR+RiqIYpIz2TDukOYRI9q9PFMvoprwJQYAwkn/JqSo\n" +
                "doQUHLfzxd6VUuWGvZgBKVr3lmtaHrzGoZynToDCQeLPJpt5fFUdnfp9+m4L5Pbw\n" +
                "NEi968xCXhCwR94XmT/uK+xeapQx/XF+duFey3Ub2lAudsPyhcx24un4S6sYC/nm\n" +
                "isKtbEhwS7NfsYHWCeTRvveHM3uOhbXaWKjPjLRqJl3S5yt+cPEqAjvgnR9uAwO4\n" +
                "FTV5lPVovm8NOaG8ulB/nZ+hK1qTXmnkQ510IDzJyd29T9XSSd1kkLJUjl7AZIOR\n" +
                "gs7szgB1CviIAb63fDJxK6NDv0GyYszoa15H83HZw6PPO+zuhW7mpxArOGrZyLJM\n" +
                "SOaKZZa/pC0vFY6IKFveqOAVGIuqegAA11T2XE69HMfLf62jWHbSjf6q4YqkAR+2\n" +
                "det5zPWhAHZjE5MY0i80o5hwm6qhJFMGn8IrXhJDbK9uRyp5W99C+p5yGA24XDpI\n" +
                "u8RZugyWUefBORt8lTIIZAB2cDBGa7sp5z9yfBo5uumRlEesVd4ZPXTmsiKntlxc\n" +
                "oprZM8+F5gT8m+S35Ft697q9aae13hPqGaCalcnfg9gsWqx94yHNx8mVTD7yMds4\n" +
                "Bol+LnL6xlRbYs9gcsELzONm+LnwKbTwI97VNJ3b43pYgSY3HtOUzL3BGK5IAFQC\n" +
                "L3q+fMFcQpczy1tdnb54cc60KBNdf2ygtuIEPPsEN3oa5sB/PYz+9gV1LdfIUGJt\n" +
                "rbutx7QyaBksOa5xwLBcFKvntm7GWlG9ftCQjOCwVOOMYlAWeZcxrNZylNjcNeT8\n" +
                "k9Ngjk8ngeSPtE5MniDXQIYogdR8CrQ94sCUs0lxhn6O4NWcySNgemaA31zdxNLh\n" +
                "ExubSVUNmn2PY6f92yx9ILHmuMO1H+UfnU/m5hy7D6f6arpJFT4tXdW9grVovYP0\n" +
                "hXCG1aiCavhDKHKhdqVrjwPxkb0eDaBm+sQgP8XNLCw8QyPiUIMtkSbJgA82ODNF\n" +
                "UGwokXbVr40yi35w2FfNqqYv/JymZ03IkSnA9avFFr1avC08LjbcyWpX+MfO8amw\n" +
                "EOkRdKeHCBwrDxJ9FHw+6XE2jOnSvGJoijnP3yP5NpadJJiPD8UHDDox81Bc7sUV\n" +
                "c4VVn+dy4MiwhauvcUOtVLobvgtHvgiDRGnSTLSeJldRc/djgk6y+zVfi6tGqkvz\n" +
                "HiOLLAo7npMj8wZhLH8vknCIu1/3I90gEsanMRFxpIal6Eg86lVz+Eggx41nl44M\n" +
                "UGrX3qcWf+IqSppBV+hiHbPyfGsQz8fymOOmuNlQVv1ZLv+0pag4dvKH9sZsC3bv\n" +
                "DdBtBp1XPfoUfY9Xf3qIiIC+jQD2zjgHQZLMM3VFdXXiF/XiFEgUpDqi5N0cu1Zn\n" +
                "S2YdRmuK2ua2duKD18Akenj7gBvE+rf4wLBdLivpnhdS1VQG2kJRCEs4dvd2tyD8\n" +
                "PKqGg1ZpWdq3rv9lU2ruwMWbL2I45CtJg1m5qfyZ4we/1YWEFtvqiod33zk04x48\n" +
                "l93DcliHBfTQsZCupf0R33uBzpu9kgOwNdMK/ybXnJOV4zQBE41xKmuCJkkP/Ks6\n" +
                "aFR0bKHnUg3bmxNsowNJrmfiM7MufXTV/lQZLWHw0veL7ieLrpt8q4M5dJ/LP1BI\n" +
                "DYUd7tW3uiaupztIoqh+6yKjZP3gUMSjNyZiLxcr15RVONkDMbHzw8ikNaZmOgR/\n" +
                "wJ3TUxyDVd5XrUGXTR4K+IEYOdqlys7eixOlYRKtKl/4kERyTPSL9wTJ1QpgUzPz\n" +
                "UcbLbOsGT9K786qWAPKhbu1yt5PSgw53E54xKMluRum1hT+l2Iz4cl76a8hQF8V0\n" +
                "9aPYrc6zHiPHYqlaJ7uImC0nbHrdWRwZ8zkbIqFDtgqSbv3VXNjSB1kvTBrYI+e+\n" +
                "H99ZkEz7RT9NIgDgzeGdBSEipgf8OaWz//mV4Pnd6/fsw7+P7gH3/HtTOD9w9m6I\n" +
                "X3kY3n049w/A+UNK52/kmPaBfj0A9N+Ezn+dIjPR3xM8f8Zh2YejfRDOvoTPf2V0\n" +
                "7wCkvQmg//p+ag7A+Tch9F9RVv/A5v9DYd+HAiT+Dwmj/btp7w0v5T4wJPG+BNLv\n" +
                "jnsFO8U+x06yvdfBv7P/Xc7/+5yn/lfc/z7a3vPT/jenR/fn4//vKHujLNU+FA76\n" +
                "/Sev3z33hgOGfZ7nmP4Qk7XUD5HtGNBu/0ls0/7AvFP7P2ZtX2MdHgAA", file.getZipBase64Content());
    }


}
