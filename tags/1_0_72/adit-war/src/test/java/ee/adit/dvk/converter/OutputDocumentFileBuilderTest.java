package ee.adit.dvk.converter;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.File;
import ee.adit.dvk.converter.containerdocument.OutputDocumentFileBuilder;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.util.Configuration;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Hendrik Pärna
 * @since 11.06.14
 */
public class OutputDocumentFileBuilderTest {

    @Test
    public void outputFilesGenerationFromContainer() throws Exception {
        ContainerVer2_1 container = new ContainerVer2_1();
        List<File> files = new ArrayList<File>();
        File file = new File();
        file.setFileSize(9756);
        file.setFileGuid(UUID.randomUUID().toString());
        file.setMimeType("application/pdf");
        file.setZipBase64Content("H4sICIrxjlMCAHRlc3RmaWxlMS5wZGYAhVoHOJvfGq+qFUWpopSmiNpJRCJq1Yq9R80SxB6VoLaq" +
                "PWrWqF2rtdWmas/aeyuqttqqVHu5/T939d7n5sn5zvf+znvecd73nHxP3g+kKoXigfLyA0Ct2a31" +
                "rVWt6QA+IAToYGINEBYGK2LsLZwsgbALRB2MsrJ1wmDBKFu0E0YKY+pghhEVBeCcsBi0HcA1BTYI" +
                "02Rf5dSEqEKulCnyKB2FU3MrXj/ipg5Vr22ppN4HYh/qvOz/Qt2CsSZ8yScgMFauSSynNvgUpgnh" +
                "VyWWC6uCQipG4BfXMhh/KRRCWH4vm1K14/o9M0INEi0KDQwtw1L7F/qWW1233l65wp121x6AsTf7" +
                "rf3y7tJgAOwvy6FQ+D8w+B/eIP6HN3+NQ4FQGBzJ/6++bbu7eeq3V/56VfsozK4ogrY7vohLIZ52" +
                "ynFpEumiyN2pcJUt+fM1LmRbWVEb9k7Puy/coBsExDfK/YcRPFZ30Vz+OX1HEvnXLNUz6uOvdkeq" +
                "61ne85Ug8cqWc6P+SLWZelsuQklSbOfDJ59GI2evfs/VFBiuP8XDvJiVCwlZxifzf3jl6SrKwQdA" +
                "yXGlnvhhjJAAEahPaOrNDRYO/fGjkk9v1yenr2fwtnp/Op2327Sf2bXiffZV/Jn/J+ezesOGubwH" +
                "V0ZcUj7H2FMXJtY8Djh33BKqEyCsZrrvI9/xc6fUYY3MyV2MioK91If+LIThg/4ZKf8U87FYR8Po" +
                "6txaj/eXo028Xg7LtKm7dNe/BrQQP2zmzt25u3vkY3qkrzRpRBh1qPGSsn5SwU+1+1Tz2a8So1AW" +
                "Y9RHVeHr5kMkbM+6zmTf6AotRqaYu48Pi2U5IlFXiBZk2ijEnmftXhcMyiJvQdMFGT18MNpKYSzk" +
                "yWBPCCpEddtbdij47jvcxJndkV8QLcw5WwBVfKAq9mOJQilGUxgTbVF5v8hy8aXBZtDEi1+xv/9I" +
                "KjCDTDlU4CGBAZ7XoIpE9HgXY/R7ZooWisXS3bAuN9nn19faNnO6jKgbw37ghTq2PV2wR8p9lXe9" +
                "OrsG8o//RYhfqqLDEtN12rVK50tWZXydmTswoQu60TLltSsKjd6WYOzffygVIRykTaODP1dKERGF" +
                "LYuapraLbo9OAJJ52bbr7gSqIf0f0aUFCgSjHHn3jZfvJL4m5m09Cm/tWU6EOHqvSpEp8JvnXeFU" +
                "u/ksvaWxlcTkvlgWXgBKLf8JVwOl2y11Y0Rhm+Xdjmgf5vThzd3pe8JgBIrmGfnBvlK83YlgvRKf" +
                "LImuvPbQVfqMb8T4GajI8r2Nq2IrZx7IM/02aXI1MhFeYaAom/fr3iTkraFFRseWb4ufFiykwrji" +
                "uoC1afWon7Kba3qnXqcRu3Z7lHu34mgfecFt/WcWoScvWWNrzQRrQ51it5fN95WwAm4hKcA3Vjx2" +
                "rxnWdF/rT7IK8/A22QQg2eczauQcC/YPpTbbcDd/WSyNyFots+lEHJXezPpFPuvHJXS9c9G5XrlF" +
                "SEo8JpgcHDLZJY8lezwRPdN9vnzFzT88K+HmMuM+3vfgHy88xfHAoL5sRJJfQir5IM/hzCHRIfLx" +
                "/bWgQ9g9wy5KA4laVR5X/yso5/In9x+rEDuE3V5yhD5349Xa0tp6dEdVONcV5aicp2ZYQngUZiT6" +
                "pGH3h/5NiGNP23zIUMhe10xU6DcIfrlxgHX4fNgjivmdDzqORIsTUn61PcAAM9Haq7Dy+MMoJTMu" +
                "831UWeSPqM6fpvvU5uvVy9wdhTk0TAeKesq0gzmuhkpeTjc/xM5Ld67bHOLEQawk7ve02KlpADbX" +
                "0m9QV5F4EOi6qrlquMqcSJ1QlQQnsuu13xniqGsLWNJzlK2X8TTyNBrKPYlbEsFkYrP22TA63x+N" +
                "gfK/52Pyv+TD80XyJfMfjW3rpA603NyNzfNIS6FyAZC13HxClMqStN5ziClcrRwEcefq5B1eqQ14" +
                "rNCRJ/lOSzCyn0VhIWWtzUBM5duIypowGmrFzJUR1B/Rn6tIebc9V8+Tpj9YZXH6ccerak748gN3" +
                "7TxftasVVF4/DRJBdf5GzQwtcplUO36fwkJYU1NeU5D2lALSaFa59JrZflzx5rZlnmi5WYFH8QM8" +
                "Wo1mBN+LXKodlJDv/hRATOxEoq0WsCtTg4cY/NR8e7azuumpXEIN09dngmZC9RQUi8XsqoccpHF5" +
                "ywmjYPKR22Z33p4QFxfHIIMzZH0ZuunR6fvcXoyBCzWJxA4kkaaHneuRAe1LgtKw9Q7GJzyz15KZ" +
                "hdDJmVqeAYVEvoXPDfao0+xnAtSHArfJhbWmdxWF/N+fEafhV7aYvEwsxRY31emE3G8TrW8IWvUP" +
                "vrXcKeINSjMBt9aUBQSiP0jI7I7i3fCBD10vYKFonos+GqStJQ3dE64RLGO0EL6y08yEfOiiZIB/" +
                "HvVCaJUHG5AZzfSIHxW6DvFKLc7wePPJqNeFF9qcvncKdVnhSvNH71WXXrVmrpb29htLjby/NTeI" +
                "l9D6vuZjY0aQdAs0EfbOP0KKPsDbYEdOh8kNaQIKGWRcD8jZpf6hmnOl5GPANx22kgX3Ery7dhHX" +
                "CD753z0oJSpf6LAlW/xePmNYXZYzbgj7NbzhphWH4/rYs/KyP02Lr7ymS0snKn2113kSZxsDF+qJ" +
                "eYVG5LzJye2VRdAfPihi44CKF6lzSvlSsVuXClQplJUb0qQeS4eF6n76Ku37NDTDMF51FDmHip+u" +
                "fcU/N/wifrPBZD1Cr+4+T4h4fuDGEnNeM283eGwmB6L7of7W3vaRSyRr+qIrtPMptlDatnzxSl7z" +
                "qCG4O9vQOopB4CmFQMXEZycWuRDn4wdGJo6U4axs9xQlWN/MnMnInnq24Ck5vKV/nTFuoooN/NAg" +
                "jpjZ1L1WFjWzsB2TwONg93agYkq4XZovb7PiaZexEXDTcPbaRg2Dru79ghqG7gFt/Gp5FunOQAdS" +
                "hoysuZz1sqduM3qY5k20aDjvoT74jc9nw5EAXuU0gwn3A027XMis8b5CoZCwZznrp/f1zvnjHTWd" +
                "frgFnaSZSdHZ4GKHVlZEUdQgw9wXq7huXieOd3GlWtITq+UCEDmPSNb7k7ST74M8pvfGza73DNb3" +
                "IZKiF0XvCBLcJht+olAwxzZhNi0y794jC7gu4MAAew86cFJyHApuplPaCla+i0+/rbmQPOrY1nfb" +
                "2jqtlxtNOQVufR6Vo0zCYlX5OWmFbyWZtDUVN3TdiOl51jJvPYu34Otir0nV9mXb+sIu+GTOYiOe" +
                "9rPz3qAR0Hyi1CubEwZI14ZZ2ajq3c6yHhAtTRmV2tiiSDvd6oG3m+PQCqRy5oyMCpEoqhNYe+Se" +
                "Lw85RzC8b64OnBv0ZRSsqIdPJ2Sw4KhnQbyRijOvVnQMYutirCQc+5Z3XYS095E+YSmcBamxPu6S" +
                "0+GBXzlfZZ/frATnO+ihHfQa6rSs6ewjv1TlYfTA+elCeWH33FCGBVFypsHmAIMw6SmEVe+DInrd" +
                "3nYxEHijuFT+hVY3SMr6Jdl2WOZsrGX5uQBRAMvqJPu8AR0NqUYxVmlnIHk8nSbNXKfp2Z7a1GiZ" +
                "uyvSMYlcgWdjt0zH7xYFTxEiTzkwrgaG2wPfEUQkRN/XcDH9uJGIN26kSb2pDOsNR1M06DjT0MKX" +
                "xAWEZy0T6INmgj4syr5t5b291Rq0Djt8tfgz8Nh10Z9Fxr7vJYPDgXFxndwtewCAFLW1HP+FxR7b" +
                "EzpWFER28n09Pn5cTd7S8OzqQDC+c1EQ6IpAgjobRmI8WocoiZgX+zK/1msFLfAw2Eaad+G8H4LH" +
                "q9GJufaglZ8T1oahxphorqzwHZtYKBoqbI5uLOoPa4SPyZL4+L24S03QpBKLyOpLDAtfkPymIIc8" +
                "ztHHM3rGyiSpUCOM1asIPti7rSNOURNGVUESYG6HQvtWlZaqe8hZS5trLWICbCpsAB4VP1uOdVun" +
                "kwHHOV5inyntrL2RWHdhDrlaPgrfmd5ByRWZA4pX6b0+/Z7Cv+JGBQ7GjqxYhe58n7QbtXzJFRt7" +
                "+5ElHZhCRrk2gc9MqTrJQDoAE2eCGcIsZWZqWUfapE7vTBfW6h4o9FlMDxh0hPIxUpKBIsoO1fVD" +
                "rezEM57R6Mg7wetE6s7rgpXfGT1Vfap9f60lemBOTk6OVHuD313EMORcLJLh+h3Prarb1+U1LSu7" +
                "4txFsG+0w3LZXhDW9jNnB61n2RflBSsZnyqHtmc+7x58fQ/8bpSZ9ZYr2eOX3nz94Td+eKW8QO3m" +
                "6Hea4+doKzySKO6jjPDs8oItKJuoB70d2v35HpnkF+l2euNHc2pbJm63pvTXFyRug3ikYLZOWABJ" +
                "ptwt2OM2mPe27ZRpYn1N3gK5r3fH0zO41qXfXL4aMfMYUbCvA/pQHaOZW05ga6zxpkA3Y702N4UK" +
                "Xs47VOodElGU1eao6WYgV/SRHqSVCwnhC5hi/06XE/FFPQ+toJo0SfWSnkbss/xomT8nGffjL1u1" +
                "gSoGsSXw5H7gL7pX6D42Wo1CwQNHA12vtL1v835JMPhOnXV/ytMPNorF3K/4XG4e75XE0MWOcFkM" +
                "PIcrKiPQ+MnYOHmPiPL6rRgFzhOtx/GrFavsWm4H6ynDFfyeFHFJMfH2TwHycRHMKRofw/03iltF" +
                "iNL1kDVvPVsZNrtTAvVdbHtilLzuu3zaX+OTlHDv14g/jFi9apfDcD73TajcXmfqZLIYJJSQvVZx" +
                "O2K2rWFJQSRZMdneDusBTq89mqV9z5uqMN9bTbdPm8Bt3Wi9nqM/Wc0Bjx5pIf6aq9sS7IF6MFAc" +
                "goOtft9KK89eafeFkWM/N1ESHSO7QvD178huNXw1lu2V686TrxF+Pf+OszOhXGQxwp+BkaLPtXDt" +
                "tnhyVBpaPn1M81Cvjjm2OHtUVZ5KyW6jyr+qrSoboZRcJTMXOxg7GTuatT4UIdR5cOimLJOWtUDV" +
                "uSGlop5QziVmSfFAIsIB9PMh7tHPp2ENsM3mNhl+imWRRLGVH7WwFyUBcz9SkKWdvizkJptaHQkZ" +
                "4WD520a5WIw2cj9eSPA9tvIB+FEsu7lsvybUylybcOaF4AqztrU27oVbnlvR94zvNbp5Avp9eAdy" +
                "5ZbwHajGwUjNnPoe0IOaWNsHABO4IfCa8EeFoi8dMaiGp0N9+KPOJmItHOPfa9uz0ZepG68ZRzcX" +
                "4AR6LvaiPLXlmQfhTHAmxjwG7meG7P16O7dMHVGfsk1QzvFxKgLy5h0jOn5StrwN9er74wvqCMYz" +
                "f8ZyyKkqLfqgTf7hF/a2kLRSzTR21oDe+wxGXRHqCA4iW897Qu8nqQNsfcp1ATDXa1X1LWXHw6Y5" +
                "Fbn8mvIKZP43iHZVm5q2xEPZ0veZF7mWxkww7+TqAaCg+ab4wtV3myDzQguLN7EBGRPQX+7eqhtx" +
                "veWiq6xR9D9GJZuF3xe2LOuZnLj25bbvfeh1qe15tt0Uc4J6WouopUl3VPrqktuWGmHX90rJM7lk" +
                "in/BfeJJmsDQC6aZVeeZ2a/1S7bvmXO6rPhZGczprmEitD/6+3tqsalRs/QoNxcrWQz433mvncUr" +
                "28v63Y0kRD4hkURbUpsNL/zNtCojxr8W7LQ9W3sz946SWV9TLpz2Wq8lSppeiH1SN0YgdmlR3bEb" +
                "9INfTXBqMSYpJoJIwR2Rdv/HYd7tsZ0G87Px/VkHEZ9vw9O1AZ8MHLdHCl6UmQ82frjPU8R60JH1" +
                "yfCLz8/pzyWxCjwMhJ5HCdNKtTkJ2wZlT/QbxirXko5yV6pWAO9BNqhMU+Xt46qxsxLPFJFIhr1r" +
                "gnnIq6guZpm5mZt5b/vG4q02K0P3X2J74ipA7wQxARiRbfUTqgca0zydhSWc3rDPSgcesHkFweSf" +
                "d1e+lSnrsozzF4zFqaVqpw69HHL4MJKfJMc4/cpKq8JgRoqh4gVHgm6xokGOYYmXynfv6dVl01Tf" +
                "bEAdqx8TH48cJwtr7lJ5fxuTw5ZgU5OKzr3ElRueo16HN3/u9sA3PhOentT6L7A8ZdLYxJEF3kSZ" +
                "QOpebHdEqVXUfUe5lOvB2MqJsqLFDHTbb8dtiZsvS9uawVjf3Q9T67SmPfNZZGihc6Yh+Oqy0ken" +
                "uDJQZvaTW2GlMnVL9baOCsY4KSv/c35sfrB8Rb61poLWc85k6ddLKlAtxMGbi+5WiYsWIS6bzyKB" +
                "5zOcqeQWbQEmYfNHgDUKXenbfX3VcGxn6SZJuslYeHhhzFQr4K5mabd2P4AgXaO7MGr4BS6cFaRn" +
                "UpjhX9uw6vLD/O3qGMP0t/jw1ASmu9M/WvEnbD/s5HrYny35nkvRPX6s4JwuMLxMa3QydeBQf3Jn" +
                "V4UecNDg14gsxxQ1nDpsKMJ/uXz+KbZy6/FJMQtWr7mhnVA22Kt6oDNvRYXbt47UKNjzi42DWgiz" +
                "4w1eWjzrUjuzdKsujZbkpSCoDeXK1S9V1NdVrxaoHroOq+wpscVqqx1/8l/7Re6OpD2pkZq/ecLO" +
                "eM1U51nB4ikdzdCK5vXyHf9fd1rlBynpWznXgIF02q3cfDdaSSCKXyPRPF4vaZSKCww8eQJJ2Knl" +
                "oIriOZC20iug0MOLubnQwFsZavztpVTbJeHoW73aBoyt1hCO47YnV/nZmSLQPLp5Jzcs2DKfNfDn" +
                "60XoBl7Pb7/DVetJ1snJ+w1XuOLK0EnCHZQ/Bw0X1Dy3uG5KrrVqSod7qxyK7n09fbc1UfLI+AIc" +
                "q+6ArKh6Pb28NWqDrECab7UmtvrfRyqbOVqPY1PaTEZsF3Rav0w8Kfx8Yh5Bxo3oq0am+SJ1cXc7" +
                "OWbNwz9rig6oRYzU/EB2mosef7tjgU2mQPYvIdIdbTE6BQuTzVuVqAhSwSqsQS+nmn5vaVLn3dbZ" +
                "hKoYtJLmfBPUKMxFUPgq1csW7p938Un0Mhg8J3kDrxzeI0AzHOLkGI8Jb8C+Stem6/dKKfEcLoHE" +
                "YR/HP58Ar++nUejf1XNAzQfEVwafI6sSNsdbbzCDPzZsFFiH5hzemuGcdbPtb070IfnouDJF1+if" +
                "vkiTVXGzEm+UyJixXweCKS5hcGs2gRb75DuIGYXQcnQ9mguA2S472j68fbDb2D35y6lNbLpSMdPd" +
                "vpaA5wyiQNWgoBtE23N1hObgB0GG3yZIpwU+x46euuPP/JLncxphTcfARN3V7zemn7bvg9DxnHs/" +
                "uAgoIu60iPY0I2yDThjNp9YjvlgzRsK7MHjLhgbzA+d2n2vNDuTZby9AoT39k3RGhTtfYEe4HYYh" +
                "zi5nW02idBa6HiMH21SvWDUpmbbPdIkmMwMFmaQsX5dFM79Ptnw2v117Qp/pZT4TMh/oiqpz7fOB" +
                "37SVAst8tzR6xlPwMkJnljb9/c4vk21a852k+m/4np3tO5h6d96q5zXffMJt5l96tGlTGSpEnYVN" +
                "gJLF6sZ5X9ouclHszu0NcDYLbRefncyRtLk6aakJ+3/w7gcxba90W0f0t3J7DC2ljw32ymQP65Zb" +
                "KPDMiVo5ZoBvzI7vxG5cxT/3xXMYvLuWzDF2fCzf9/Norlk8b0pgjWFyazNJV2jh2zHibJziyJ1o" +
                "KSu/RjVUrkD7OyGhY72mKO6Ym8PnXSMOCKHToeVe0NaIMyitAT2YFmN3y1l4Vt1TPSRUKg4oPDtt" +
                "alCGctigC72b3rd/qn6sV2ef2jhy/5ZCny+5T3pF26cpxrUjchq5nvpmPJ83n7iKdsmPnNEGw1sf" +
                "a3PSmqIqht4r0dMfyjXbh1sbZoDZCfDVE3xKG2lvzTfw5vk0RbYoj56UHpHLnzf4vvhpPDgBcrZt" +
                "RYvm0Xodv+NQbV6hWxj+6ftwQtVCtQVZ5WQK1BA23tzZ3GLcaGFV2ANe9fowk+3p47s8M+mLU3n3" +
                "9ezNfK0brf75M4eoopic1ef4CE9ssZun9HfvQ6pDO7PaO7tZE8VN8Q3zz6pt3Knqh3LOCFkN1K4Y" +
                "3rsLOKYgyOH9ZB5vXrF+fHSsMjCoKKyJ71nu/h3RZCdfoRMoohN8d9dUqFjfN+D2nJ4tqTPJmT/l" +
                "otUi+zB6MXj3pJGp7Zf7jV+b4j9KiPZ+6VMsWb35TLD1BY6Dzs2MbLk7puWtsokJNpr3P66YQlSC" +
                "zcPK29kcX7ty7H+0Yjp9Ml+SjCHZ7xMOG/owr2SGvevdYf7teLPT4E5nw81nskxbAWH15JJhUWIr" +
                "/LoqwiDqJg5WTurwurLCoADhezZOaWOMNFum3Xk5p9Mm4i0qraAUhS58WaLoCOaQUu0uVC3ed/Ls" +
                "G8Pi2Vmy96Ne0skiJuRl8bLPZd8korqouLNixlST7yc6cp/x59MOL3KrX5O7wrVPlZ0jaxN9DNyW" +
                "UccKsZEAtx8+CHWKsJmVOF0zxEGOD4f03WDjI/of389bVtuR7UQYrodUhSXIzJz2ZJprqZVPYdT2" +
                "egmGECXs02yr8YE6yW95RLST2bIXmac6Z72X/UWJ3CWaAN8AeUAWNfHbsTUBy9CgcqdfauLXmFV9" +
                "F0SqUBEkZpo23EVK2ZTuNDHN8r8kBqTgh72J/Tp1jcfYU/I7Ug9CWQef7ronN5AJrXHE45bhsmcX" +
                "/jLKPAjLzn6z+yBkQv+QZ3BE4PW1PEIrnmHvC98tojUPlZYJT4mORQCr1yeAaqdEF5SHwfhO9OY6" +
                "Q/B8hoOxZSNT0VtukV7fAlbCwlpoVNpt/G6rHPTpxzULAh015foRHTUVgCDk7gqxYFZteRG6mV9v" +
                "uzpw/W4na8LR/LTuoVZUNDtqn9Pd+xZrNkZ2W/ZcVl6q5J47mYTsU+a5MFYWdNw9AaDcIUE2bfRj" +
                "P11Z+wsma3cT6jn/9T211as2dvRer34KZZt2GGU2LmyHHbrN/wz9mVLd2IZgmKFdO8ia0n6rJCGy" +
                "QIdfRWiroFqSIcaAY9VnqUS5U+MspB8/1/SroL7wlYkVJCKPQ70v7EZ2x+4+px/EUosubEs+aaSf" +
                "FdGuLicnVTXp1XXmYDY7yzlX3nD5+oHhfbjgTg0ppWk/g4NkW+IBgxAldGVrLjwYavGIgrkjpWCC" +
                "2aS3toKAsgzyYfV+hLBN+zCZVP7YCE9izl9f4DDP1vMBKZqZbzKkpFDTs9ni5xI2a8QkItISfaxO" +
                "YiTfZdceUE3LSYyxOsFJamW7JCSmWJfZXt/vNlCUDq7pfOPMNcdG5e7r7y0l4ce6zPragAobFSXF" +
                "OYu2ZvhgS+5A7wrT7kEwViX0d47IqNglqjGM15MN+Gd1RbJIuw68QKkrj06m9E2nvGtJsvQx17LK" +
                "cKH0oiESDate1EWGsC6RudP0rOmmLOumPIysv8wGaHa5LOXFk82q8YIHcoIFLJ4vrhdWgb/OHRMT" +
                "hKMJkQDSLIkz6chxr+UUyqS3h53rjKLsvnL7tS/wcVMd6wSLJnP3RZiNAGQVWm8KTr1FBIYcD76Z" +
                "bvTJD/CfvuZONIev68ryNbzSAyXWUxbY/Q2VqqJno9H99J5+Wz5L3AJr7WR/gPg8mCgsqRJj/DoS" +
                "rf/AWjeg9TCrn3mTMtM0i1xXrLze5ryJKpdYRT+t4qc2IMxs9JThpbBL12k+pvooAYSGy6dhA0kQ" +
                "Mqz27pN8E6dZtDXryppbDJWPO5Mkx214SIsYuA0WwziLmdrS9B3sXIqreFu+b/Awan8D9rt16ZnW" +
                "TusxVnJamJLrWqCJi9m524xJuWfzRJJVasqYFevmpPM/5GuPqV/8O62uFf0xtyy/TPNVPrVz2XTG" +
                "eB58Q6pSr0gzzDqf0BlwFu0X5kWUmslfiuLg48XaZoSPrE1svnzLYUC+f/11CKJI52HSxUPy4Oi9" +
                "7vTC0iC5pf2xspaGZ13dSoOjZQ/TNq0BBcx92IACOW2e9/nO/lFCwuxx6zzoqzxFaCp0puGrLQHb" +
                "IN8bn4tnYLrJjZsukUSKVsk2ivDugxdxL45o0dtQ0S+fGHOtkArzMxWZWt78PSwJfDe69/m7P5mI" +
                "YYUURZlX6TcoYhvLOkbcdRJVRK0tzbFo7w+dI9RsBTWMsssllkG3Dh7Zv+kggpXjy6UyVs7QVva0" +
                "71muGpNNg1gf8lb3KMkuuDCjXVj8IgJPKYP8ZrIVIF12hCM5eQjwcqaSPW+1HGkz6exKQMWN8uhV" +
                "3M0wAqUynll/s2cByTqyhAK6zs3ZrOZlHsNmAt9Yo0gR8W9fHGvOojwAdga/5hIcfHukO1bcHsxM" +
                "Ulyb2SYTzXb5Pt43xyY4ek+OJTVtEkyUus2YOld4dGoWOfcI/HR6MyfazY5WrRp6JOxxTlwBr6iI" +
                "qKj1Oso6mD2g35p3H5hswWBujXhupx1qVW0xx/KYxsZBjUi5eDOLjTcCeh8RHGg1e/I4gZcbnk2c" +
                "lI1Twrl3RyulZ6BJL4OPptOsDrhlYYcDq+1CA+Z1vAfEDfqCV+R4IwvfgaMqZRW1DPQNejRr3iTK" +
                "d3oT0Gu8Kk626BGWIG/TJKRm2CZtunWH/s5VDb9G0VOBYDA1QZBOS9Ys/MQZPArvsnc4N76RIcdr" +
                "MMQ6wW009yBT/XVFKo9ccWhPTAx9S7Sp2qIitTb0NVCRnk8XHgtibbv+a+8nRfv0gypSicNu6FI/" +
                "mzjdITHWRYO0PSsDdIx7z3LyOFSEiVCkpkl8idlvT9AE/d7gnY8Tvj4u5bkklLpHGjKOklmKanaV" +
                "ZzPi1i3uDidwlDgBpwKzFf9LgRjxVzFYAAFB/gMU+GeFWNPtCQaMcrB3ksLgTLFWT5wcsH8nldF2" +
                "GLCE+OWHSxxrhbZV0gRclo4tcEB+wN85JCQcXPV5EAh+IA+Mjx/IB4FAgFAIBGEIlnNC21qZittb" +
                "2GKAEABYHGeKsXcCCkLgAPCFlr8TfFAoACyJfiKLsbKwdLqYdzmo4YSx0wYiIb/lX5SrMXxA+GXp" +
                "GiAq+g/bkX9Ut/lg8P9TqTdUbVZuA1LXH9zaqWFCDI6v5dyD2iDWmw1Nj/DZ67WpSJfjmE6/XTWE" +
                "7115hTZ/HNipModbtV0uWXaOpERtXl9cejRMjK88/i6f0NS5/h12U5egQ6PqU3wwbhX1qSq8EWQX" +
                "UtG82H2Ct0PXGCNeZv+8tTNPpd5I8qTltuG15uxrfqZJB5kt0n1dChuTr1hnahg1lgd2Gwj863at" +
                "SzI/NIPYX2TP+hfekpDwEFUI7P8xkiQCDT/VUMNqd3O0SYJuFpN62/LZH3874/u80j+y/9RH7/nj" +
                "EPXWslh1noQycp8JGs+uPGwD3hwAS/lfEkDwv8QarOFs4nRJaGKdMX9HJdA4zOXInyG3wuKcJC3R" +
                "2MtIKqL/uocBwI+szJwscfoCcAgQDkcA4ZDfvSHgP7IJKPD3CII1HbTsrS4DA0T+Z0ihkH/aiIIC" +
                "Bf8Yh/5j/FL4Zbr8lqmKdTDVwDjpgy9e6QBrYlydDP9t2n94roq2uLxgL/OP/+9vRKhjcA7OWFMM" +
                "Dnih4hJQwphZoS/z+lIBXBAORPLzGYJlsA7OTy6kaFysF9oe9+RShKkbWFLjIp1drEwx6jISYDmg" +
                "08VaioqCJS8MvNCAA/JdSvwXc/j/izk4wB82AP5pBPBfrLhcWAUrM5w+8O9cF+SFJufLtfhXj/n+" +
                "Q4ck+mIrOlj81gXk/y1f5QnGXtzUycrBXv+3LLCOrh7Q3tnW9vcFYngZaXsLdowTj7Q0x78tKeyf" +
                "CsSdnSwdsMIoaRQKAuFHXux8+EWTvmj8EIgA30UveNEkIJDLkwF+0aT/jv/FAxW9cOAiUZ3+IQIu" +
                "8M9pAvyX4i5p0b9H2czZFPMPVRftgvUvdb9pBOKy/aUS9nuM71IN6i+ZAr/NgMEu8cv+L/WXqyB1" +
                "cWawSz3gg0D5IQgIPxQG5Yfxc0Fg9yGQ+xz/4rwrFmMOuDjj+AGQf3yACDgcBgeaA//CkAgkFPh7" +
                "xB74Dz6o4H9gl9bA/xNDIvn4/+Tj+4NPQBCK/BODCf4hD8on8AfGD4P+gcEF+f/AEHyIPzBBPth/" +
                "YoIXjvwTc8KiLw5tLOByr1i5Yy6WCqzu4HCRo3y/M0/O3tzh4l2jvwgpoD5QGCIljoIhBGD8MAgC" +
                "KSkOlxIXlETABcThfChBAXGUpCjg/7NcbgYpB1NJS4ypDc7ZDgiGIRAIaSkpKBx6EXwJPikBpAAC" +
                "KiAtBZfklxQQ54cA/v7jgMY6/T2kgnwCMAAIJK2CAvwNJUPCXhwmAAA=");
        files.add(file);
        container.setFile(files);

        Configuration configuration = new Configuration();
        configuration.setTempDir(System.getProperty("java.io.tmpdir"));

        OutputDocumentFileBuilder builder = new OutputDocumentFileBuilder(configuration, container);

        List<OutputDocumentFile> outputDocumentFiles = builder.build();
        Assert.assertNotNull(outputDocumentFiles);
        Assert.assertEquals(1, outputDocumentFiles.size());
    }
}
