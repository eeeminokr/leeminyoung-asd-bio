package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.entity.VideoResource;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface IVideoResourceRepository extends IEntityRepository {
        class VideoResourceSqlProvider implements ProviderMethodResolver {

                public String findAll(long projectSeq, String subjectId, String orgId, String gender,
                                String interfaceId,
                                int page, int offset) {
                        return new SQL() {
                                {
                                        SELECT("T.SubjectId,T.OrgId,T.OrgName, T.ProjectSeq,P.ProjectName ,T.OrgName,T.Name, T.Gender,V.CDate as dateCreated, T.Registdate as Registdate, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay , T.RegistDate) ,0) AS TrialBirthDay, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay , T.RegistDate) ,0) as BirthDay,COALESCE(V.TrialIndex,0) as TrialIndex,COALESCE( V.InterfaceId, 'NO_DATA') InterfaceId");
                                        FROM("T_DataSummary T");
                                        LEFT_OUTER_JOIN(
                                                        "T_VideoResource V  ON V.SubjectId = T.SubjectId  AND V.Published = 1 AND V.Deleted!= 1");
                                        LEFT_OUTER_JOIN("T_Project P ON P.ProjectSeq = T.ProjectSeq");
                                        WHERE("T.State <> 4");
                                        if (!Objects.isNull(subjectId)) {
                                                AND().WHERE("T.SubjectId=" + subjectId);
                                        }
                                        if (projectSeq != 0) {
                                                AND().WHERE("T.ProjectSeq= " + projectSeq);
                                        }
                                        if (!Objects.isNull(orgId)) {
                                                AND().WHERE("T.OrgId= " + orgId);
                                        }
                                        if (StringUtils.hasText(gender)) {
                                                AND().WHERE("T.Gender=  #{gender}");
                                        }
                                        if (StringUtils.hasText(gender)) {
                                                AND().WHERE("V.InterfaceId=  #{InterfaceId}");
                                        }
                                        ORDER_BY("T.CDate DESC ,T.SubjectId, V.InterfaceId ");
                                        // LIMIT(offset);
                                        // OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                                }
                        }.toString();

                }

                // public String getPagingList(long projectSeq, String subjectId, String orgId,
                // int page, int offset){
                // return new SQL(){{
                // SELECT("T.SubjectId");
                // FROM("T_DataSummary T");
                // LEFT_OUTER_JOIN("T_VideoResource V ON V.SubjectId = T.SubjectId AND
                // V.Published = 1 AND V.Deleted!= 1 AND V.ProjectSeq !=3");
                // WHERE("T.ProjectSeq != 3");
                // if (!Objects.isNull(subjectId)) {
                // AND().WHERE("T.SubjectId=" + subjectId);
                // }
                // if(projectSeq != 0){
                // AND().WHERE("T.ProjectSeq= " + projectSeq);
                // }
                // if(!Objects.isNull(orgId)) {
                // AND().WHERE("T.OrgId= "+ orgId);
                // }
                // GROUP_BY("T.SubjectId ,V.TrialIndex");
                // LIMIT(offset);
                // OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                // }}.toString();

                // }

                public String getTotalCount(long projectSeq, String subjectId, String orgId, String gender) {
                        return new SQL() {
                                {
                                        SELECT("T.SubjectId");
                                        FROM("T_DataSummary T");
                                        LEFT_OUTER_JOIN(
                                                        "T_VideoResource V  ON V.SubjectId = T.SubjectId  AND V.Published = 1 AND V.Deleted!= 1");
                                        WHERE("T.State <> 4");
                                        if (!Objects.isNull(subjectId)) {
                                                AND().WHERE("T.SubjectId=" + subjectId);
                                        }
                                        if (projectSeq != 10) {
                                                AND().WHERE("T.ProjectSeq= " + projectSeq);
                                        }
                                        if (!Objects.isNull(orgId)) {
                                                AND().WHERE("T.OrgId= " + orgId);
                                        }
                                        if (StringUtils.hasText(gender)) {
                                                AND().WHERE("T.Gender=  #{gender}");
                                        }
                                        GROUP_BY("T.SubjectId ,V.TrialIndex,V.ProjectSeq");

                                }
                        }.toString();

                }

                public String queryVideoResources3(String systemId, String subjectId, long projectSeq) {
                        return new SQL() {
                                {
                                        SELECT("VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled");
                                        FROM("T_VideoResource");
                                        WHERE("Deleted=false");
                                        if (StringUtils.hasText(systemId)) {
                                                AND().WHERE("SystemId= #{systemId}");
                                        }
                                        if (!Objects.isNull(subjectId)) {
                                                AND().WHERE("SubjectId=" + subjectId);
                                        }
                                        if (projectSeq != 0) {
                                                AND().WHERE("ProjectSeq= " + projectSeq);
                                        }
                                        ORDER_BY("RetryIndex");

                                }
                        }.toString();
                }

        }

        @SelectProvider(VideoResourceSqlProvider.class)
        public List<VideoResource> findAll(long projectSeq, String subjectId, String orgId, String gender,
                        String interfaceId, int page, int offset);

        // @SelectProvider(VideoResourceSqlProvider.class)
        // public List<VideoResource> getPagingList(long projectSeq,String subjectId,
        // String orgId, int page,int offset);

        @SelectProvider(VideoResourceSqlProvider.class)
        public List<VideoResource> getTotalCount(long projectSeq, String subjectId, String orgId, String gender);

        /**
         * 신규 영상파일 업로드 정보를 저장한다.
         * 
         * @param videoResource
         */
        @Insert("INSERT INTO T_VideoResource (VideoId, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, AnsweredYn, RetryIndex, OriginalFileName, TargetFileName, Hostname, Deleted, CDate, CUser, UDate, UUser) "
                        + " VALUES(#{id}, #{orgId}, #{systemId}, #{projectSeq}, #{subjectId}, #{trialIndex}, #{interfaceId}, #{answeredYn},#{retryIndex}, #{originalFileName}, #{targetFileName}, #{hostname}, #{deleted}, #{dateCreated}, #{userCreated}, #{dateUpdated}, #{userUpdated})")
        public void add(VideoResource videoResource);

        /**
         * @param id video id
         * @return
         */
        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE VideoId = #{videoId}")
        public Optional<VideoResource> findById(@Param("videoId") long id);

        /**
         * @param published
         * @return
         */
        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Failed=false and Deleted=false and Hostname = #{hostname} and Published = #{published}")
        public List<VideoResource> queryByPublished(@Param("hostname") String hostname,
                        @Param("published") boolean published);

        /**
         * Encrypted video file을 publish하고 VideoResource정보를 업데이트함.
         * 
         * @param videoResource
         * @return
         */
        @Update("UPDATE T_VideoResource "
                        + " SET Failed=#{failed}, Published=#{published}, Labelled=#{labelled}, PublishedError=#{publishedError}, PDate=#{datePublished} ,LDate=#{dateLabelled} "
                        + " WHERE VideoId=#{id}")
        public boolean changeAfterPublishedAndLabelled(VideoResource videoResource);

        /**
         * 
         * @param systemId
         * @param subjectId
         * @param interfaceId
         * @return
         */
        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and SystemId = #{systemId} and SubjectId = #{subjectId} and InterfaceId =#{interfaceId} order by TrialIndex desc, RetryIndex")
        public List<VideoResource> queryVideoResources1(@Param("systemId") String systemId,
                        @Param("subjectId") String subjectId, @Param("interfaceId") String interfaceId);

        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex} and InterfaceId =#{interfaceId} order by RetryIndex")
        public List<VideoResource> queryVideoResources2(@Param("systemId") String systemId,
                        @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,
                        @Param("trialIndex") int trialIndex, @Param("interfaceId") String interfaceId);

        @Select("SELECT V.ProjectSeq AS ProjectSeq, T.ProjectSeq AS SubjectProjectSeq, T.VideoResource,V.VideoId as id, V.OrgId, V.SystemId, V.ProjectSeq as ProjectSeq, V.SubjectId, V.TrialIndex, V.InterfaceId as InterfaceId, V.RetryIndex, V.OriginalFileName as OriginalFileName, V.TargetFileName as TargetFileName, V.Published, "
                        + "V.PublishedError, V.Labelled as Labelled, Deleted, V.CDate as DateCreated, V.CUser as UserCreated, V.UDate as DateUpdated, V.UUser as UserUpdated, V.PDate as DatePublished, V.LDate as DateLabelled, V.CPDate, V.Labelled"
                        + "  FROM T_VideoResource V"
                        + " INNER join T_DataSummary T ON T.SubjectId = V.SubjectId AND T.TrialIndex = V.TrialIndex   AND T.ProjectSeq != V.ProjectSeq"
                        + " WHERE T.State <> 4 AND V.Published = 1 AND V.Deleted != 1   AND V.TrialIndex =1 AND V.ProjectSeq = #{projectSeq} AND V.SubjectId = #{subjectId}")
        public List<VideoResource> findByproectSeqLists(@Param("projectSeq") long projectSeq,
                        @Param("subjectId") String subjectId);

        @Select("SELECT V.ProjectSeq AS ProjectSeq, T.ProjectSeq AS SubjectProjectSeq, T.VideoResource,V.VideoId as id, V.OrgId, V.SystemId, V.ProjectSeq as ProjectSeq, V.SubjectId, V.TrialIndex, V.InterfaceId as InterfaceId, V.RetryIndex, V.OriginalFileName as OriginalFileName, V.TargetFileName as TargetFileName, V.Published, "
                        + "V.PublishedError, V.Labelled as Labelled, Deleted, V.CDate as DateCreated, V.CUser as UserCreated, V.UDate as DateUpdated, V.UUser as UserUpdated, V.PDate as DatePublished, V.LDate as DateLabelled, V.CPDate, V.Labelled"
                        + "  FROM T_VideoResource V"
                        + " INNER join T_DataSummary T ON T.SubjectId = V.SubjectId AND T.TrialIndex = V.TrialIndex   AND T.ProjectSeq != V.ProjectSeq"
                        + " WHERE T.State <> 4 AND V.Published = 1 AND V.Deleted != 1   AND V.TrialIndex =1 AND V.ProjectSeq = #{projectSeq} ")
        public List<VideoResource> findByproectSeqForcedLists(@Param("projectSeq") long projectSeq);

        @Update("UPDATE T_VideoResource "
                        + " SET Deleted=true, UDate=#{dateUpdated}, UUser=#{userUpdated} "
                        + " WHERE VideoId = #{id}")
        public boolean delete(VideoResource resource);

        // @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId,
        // TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName,
        // Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as
        // UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as
        // DatePublished, LDate as DateLabelled "
        // +" FROM T_VideoResource "
        // +" WHERE Deleted=false and SystemId = #{systemId} and SubjectId =
        // #{subjectId} and ProjectSeq=#{projectSeq} order by RetryIndex")
        @SelectProvider(VideoResourceSqlProvider.class)
        public List<VideoResource> queryVideoResources3(@Param("systemId") String systemId,
                        @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq);

        /**
         * After labelling, VideoResource정보를 업데이트함.
         * 
         * @param videoResource
         * @return
         */
        @Update("UPDATE T_VideoResource "
                        + " SET ProjectSeq=#{projectSeq}, TargetFileName=#{targetFileName}, Labelled=#{labelled}, LDate=#{dateLabelled} "
                        + " WHERE VideoId=#{id}")
        public boolean changeAfterLabelled(VideoResource videoResource);

        @Update("UPDATE T_VideoResource "
                        + " SET ProjectSeq=#{projectSeq},OriginalFileName=#{originalFileName}, TargetFileName=#{targetFileName}, Labelled=#{labelled},UDate=#{dateUpdated}, LDate=#{dateLabelled}, CPDate=#{dateChangeProject} "
                        + " WHERE VideoId=#{id}")
        public boolean changeAfterFileNameLabelled(VideoResource videoResource);

        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and Published=true and SystemId = #{systemId} and SubjectId = #{subjectId} and TrialIndex=#{trialIndex} order by InterfaceId, RetryIndex")
        public List<VideoResource> findBySubjectIdAndTrialIndex(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("trialIndex") int trialIndex);
        
        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and Published=true and SystemId = #{systemId} and SubjectId = #{subjectId} and TrialIndex=#{trialIndex} and InterfaceId=#{ifId} order by RetryIndex desc")
        public List<VideoResource> findBySubjectIdAndTrialIndexAndIfId(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("trialIndex") int trialIndex, @Param("ifId") String ifId);


        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and Published=true and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex} order by InterfaceId, RetryIndex")
        public List<VideoResource> findByProjectSeqAndTrialIndex(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,@Param("trialIndex") int trialIndex);
                
        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and Published=true and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex} and InterfaceId=#{ifId} order by RetryIndex desc")
        public List<VideoResource> findByProjectSeqAndTrialIndexAndIfId(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex, @Param("ifId") String ifId);

        @Select("SELECT VideoId as id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, InterfaceId, RetryIndex, OriginalFileName, TargetFileName, Published, PublishedError, Labelled, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, LDate as DateLabelled "
                        + " FROM T_VideoResource "
                        + " WHERE Deleted=false and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} order by RetryIndex desc")
        public List<VideoResource> findByProjectSeq(@Param("systemId") String systemId,
                        @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq);

        @Select("SELECT V.VideoId as id, V.OrgId, V.SystemId, V.ProjectSeq as ProjectSeq, V.SubjectId, V.TrialIndex, V.InterfaceId as InterfaceId, V.RetryIndex, V.OriginalFileName as OriginalFileName, V.TargetFileName as TargetFileName, V.Published, V.PublishedError, V.Labelled as Labelled, Deleted, V.CDate as DateCreated, V.CUser as UserCreated, V.UDate as DateUpdated, V.UUser as UserUpdated, V.PDate as DatePublished, V.LDate as DateLabelled, T.VideoResource as VideoResourceState "
                        + " FROM T_VideoResource V "
                        + " LEFT JOIN T_DataSummary T ON T.SubjectId = V.SubjectId and V.ProjectSeq = T.ProjectSeq AND T.TrialIndex = V.TrialIndex "
                        + " WHERE T.State <> 4 AND V.Published = 1 AND V.Deleted != 1   AND Labelled = false AND T.ProjectSeq = #{projectSeq} ORDER BY V.SubjectId,V.InterfaceId")
        public List<VideoResource> findByLabelledViedoResource(@Param("projectSeq") long projectSeq);

        @Update("UPDATE T_VideoResource "
                        + " SET ProjectSeq=#{projectSeq}, OriginalFileName=#{originalFileName}, TargetFileName=#{targetFileName} , CPDate=#{dateChangeProject} "
                        + " WHERE VideoId=#{id}")
        public void updateProjectSeq(VideoResource videoResource);
}
