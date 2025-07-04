package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.entity.Board;
import com.ecoinsight.bdsp.asd.entity.BoardComment;
import com.ecoinsight.bdsp.asd.entity.BoardFile;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface IBoardRepository extends IEntityRepository {
  class BoardSqlProvider implements ProviderMethodResolver {
    public static String findAllBoard(final String boardCode, final String orgId, final String userId, final String title, final String all,final int page,
        final int offset) {
      return new SQL() {
        {
          SELECT(
              "b.BoardSeq boardItemId, b.BoardCode, b.Title, b.Contents,b.UserId,b.WriterName,o.OrgId,o.OrgName, b.CUser userCreated, b.CDate dateCreated, b.UUser userUpdated, b.UDate dateUpdated,b.NewBstate,b.NewMstate, (select count(BoardCommentSeq) from T_BoardComment where BoardSeq = b.BoardSeq and PCommentSeq = 0) commentCount");
          FROM("T_Board b");
          INNER_JOIN("T_Organization o on  o.OrgId = b.OrgId");
          INNER_JOIN("T_BoardSettings s on (s.BoardCode = b.BoardCode and s.Status ='ACTIVE' )");
          WHERE("b.BoardCode = #{boardCode}");
            if (!Objects.isNull(orgId)) {
            AND().WHERE("b.OrgId= "+ orgId);
            } 
            if (StringUtils.hasText(userId)) {
            AND().WHERE(String.format("b.UserId LIKE  '%%%s%%'", userId));
          } else if (StringUtils.hasText(title)) {
            AND().WHERE(String.format("b.Title LIKE  '%%%s%%'", title));
          } else  if(StringUtils.hasText(all)){
            AND().WHERE(String.format("b.UserId LIKE  '%%%s%%'", all));
            OR().WHERE(String.format("b.Title LIKE  '%%%s%%'", all));
          }

          ORDER_BY("b.UDate DESC");
          LIMIT(offset);
          OFFSET(page <= 1 ? 0 : (page - 1) * offset);
        }
      }.toString();
    }

    public static String getTotalCount(final String boardCode, final String orgId, final String userId, final String title,final String all) {
      return new SQL() {
        {
          SELECT("COUNT(b.BoardSeq)");
          FROM("T_Board b");
          INNER_JOIN("T_Organization o on  o.OrgId = b.OrgId");
          INNER_JOIN("T_BoardSettings s on (s.BoardCode = b.BoardCode and s.Status ='ACTIVE' )");
         
          WHERE("b.BoardCode = #{boardCode}");
          if (!Objects.isNull(orgId)) {
            AND().WHERE("b.OrgId= "+ orgId);
            }
          if (StringUtils.hasText(userId)) {
            AND().WHERE(String.format("b.UserId LIKE  '%%%s%%'", userId));
          } else if (StringUtils.hasText(title)) {
            AND().WHERE(String.format("b.Title LIKE  '%%%s%%'", title));
          } else if (StringUtils.hasText(all)) {
            AND().WHERE(String.format("b.userId LIKE  '%%%s%%'", all));
            OR().WHERE(String.format("b.Title LIKE  '%%%s%%'", all));
          }
        }
      }.toString();
    }
  }

  /* 모바일 사용자 mapper */

  /**
   * 
   * @param board
   */
  @Insert("INSERT INTO T_Board(BoardCode, Title, Contents,UserId, WriterName, Password, OrgId,OrgName, CUser, CDate, UUser, UDate,NewBstate, NewMstate)" 
       + "VALUES(#{boardCode}, #{title}, #{contents},#{userId}, #{writerName}, #{password}, #{orgId},#{orgName}, #{userCreated}, #{dateCreated}, #{userUpdated}, #{dateUpdated},#{newBstate},#{newMstate})")
  @SelectKey(statement = "select last_insert_id()", keyProperty = "boardItemId", before = false, resultType = long.class)
  @Options(useGeneratedKeys = true, keyProperty = "boardItemId")
  public boolean add(Board board);

  @Update("UPDATE T_Board SET Title=#{title}, Contents=#{contents}, UUser=#{userUpdated}, UDate=#{dateUpdated} WHERE BoardSeq=#{boardItemId}")
  public int changeOneMboardItem(Board item);

  @Delete("DELETE FROM T_Board WHERE BoardSeq =#{boardItemId}")
  public long deleteMboardFiles(@Param("boardItemId") final long boardItemId);

  @Insert("INSERT INTO T_BoardFile (BoardSeq, FileName, FileSize, FileExtension, BaseUrl, FileUrl, FilePath, CUser, CDate)"
      +
      "VALUES(#{boardItemId}, #{fileName}, #{fileSize}, #{fileExtension}, #{baseUrl}, #{fileUrl}, #{filePath}, #{userCreated}, #{dateCreated})")
  public int addOneBoardFile(BoardFile file);

  /**
   * @param boardCode
   * @return
   */
  @SelectProvider(BoardSqlProvider.class)
  // @Select("select b.BoardSeq boardItemId, b.BoardCode, b.Title,
  // b.Contents,b.WriterName, b.CUser userCreated, b.CDate dateCreated, b.UUser
  // userUpdated, b.UDate dateUpdated "
  // + " , (select count(*) from T_BoardComment where BoardSeq = b.BoardSeq and
  // PCommentSeq = 0) commentCount "
  // + " from T_Board b inner join T_BoardSettings s on (s.BoardCode =
  // b.BoardCode)"
  // + " where s.Status ='ACTIVE' and b.BoardCode = #{boardCode} and b.WriterName
  // like '%' + #{name} + '%' order by b.UDate desc limit #{offset} offset
  // #{page}")
  public List<Board> findAllBoard(@Param("boardCode") String boardCode,@Param("orgId") String orgId,@Param("userId") String userId,
      @Param("title") String title, @Param("all") String all,final int page, final int offset);

  // @Select("select count(b.BoardSeq)"
  // + " from T_Board b inner join T_BoardSettings s on (s.BoardCode =
  // b.BoardCode)"
  // + " where s.Status ='ACTIVE' and b.BoardCode = #{boardCode} and b.WriterName
  // like '%#{name}%'")
  @SelectProvider(BoardSqlProvider.class)
  public long getTotalCount(@Param("boardCode") String boardCode,@Param("orgId") String orgId, @Param("userId") String userId,
      @Param("title") String title,@Param("all") String all);

  // /**
  // *
  // * @param boardItemId
  // * @param password
  // * @return
  // */
  // @Select("SELECT b.Password"
  // + " FROM T_Board b inner join T_BoardSettings s on (s.BoardCode =
  // b.BoardCode)"
  // + " WHERE b.BoardCode = #{boardCode} and b.BoardSeq = #{boardItemId} and
  // b.Password = #{password} and s.Status = 'ACTIVE'")
  // public long findByPassword(@Param("boardItemId") final long boardItemId);

  /**
   * 
   * @param boardCode
   * @param boardItemId
   * @return
   */

  default public Optional<Board> findByboardItemId(final String boardCode, final long boardItemId) {
    return findyOnOneboard(boardCode, boardItemId);
  }

  /**
   * 하나의 board 찾기
   * 
   * @param boardCode
   * @param boardItemId
   * @return
   */
  @Select("SELECT b.BoardSeq boardItemId, b.BoardCode, b.UserId,b.Password,b.Title, b.Contents, b.WriterName,o.OrgId,o.OrgName, b.CUser userCreated, b.CDate dateCreated, b.UUser userUpdated, b.UDate dateUpdated, b.NewMstate newMstate, b.NewBstate newBstate"
      + " FROM T_Board b"
      + " inner join T_Organization o on o.OrgId = b.OrgId"
      + " inner join T_BoardSettings s on (s.BoardCode = b.BoardCode)"
      + " WHERE b.BoardCode = #{boardCode} and b.BoardSeq = #{boardItemId} and s.Status = 'ACTIVE'")
  public Optional<Board> findyOnOneboard(@Param("boardCode") final String boardCode,
      @Param("boardItemId") final long boardItemId);

  /**
   * boardSeq로 파일 찾기
   * 
   * @param boardItemId
   * @return
   */
  @Select("SELECT BoardFileSeq fileId, FileName, FileSize, FileExtension, BaseUrl, FileUrl, FilePath, CUser userCreated, CDate dateCreated"
      + " FROM T_BoardFile"
      + " WHERE BoardSeq = #{boardItemId}")
  public List<BoardFile> findByboardSeqFile(@Param("boardItemId") final long boardItemId);

  /**
   * 
   * 
   * @param boardCode
   * @param fileId
   * @return
   */
  @Select("SELECT f.BoardFileSeq fileId, f.FileName, f.FileSize, f.FileExtension, f.BaseUrl, f.FileUrl, f.FilePath, f.CUser userCreated, f.CDate dateCreated"
      + " FROM T_BoardFile f"
      + " inner join T_Board b on (b.BoardSeq=f.BoardSeq)"
      + " inner join T_BoardSettings s on (s.BoardCode = b.BoardCode)"
      + " WHERE b.BoardCode = #{boardCode} and s.Status = 'ACTIVE'"
      + " and f.BoardFileSeq = #{fileId}")
  public BoardFile findByfileSeqFile(@Param("boardCode") final String boardCode,
      @Param("fileId") final long fileId);

  /**
   * 
   * @param boardItemId
   * @param parentCommentId
   * @return
   */
  @Select("SELECT BoardCommentSeq boardCommentId, BoardSeq boardItemId,Contents,UserId,WriterId, WriterName, PCommentSEq parentCommentId, CUser userCreated, CDate dateCreated, UUser userupdated,UDate dateUpdated"
      + " FROM T_BoardComment WHERE BoardSeq = #{boardItemId} and PCommentSeq =#{boardCommentId}")
  public List<BoardComment> findByPCommentId(@Param("boardItemId") final long boardItemId,
      @Param("boardCommentId") final long boardCommentId);

  @Select("SELECT BoardCommentSeq boardCommentId, BoardCommentSeq boardItemId,Contents,UserId,WriterId, WriterName, PCommentSEq parentCommentId, CUser userCreated, CDate dateCreated, UUser userupdated,UDate dateUpdated"
      + " FROM T_BoardComment WHERE BoardSeq = #{boardItemId} and BoardCommentSeq =#{boardCommentId}")
  public BoardComment findByBoardCommentId(@Param("boardItemId") final long boardItemId,
      @Param("boardCommentId") final long boardCommentId);

  // Board File

  @Select("SELECT f.BoardFileSeq fileId, f.FileName, f.FileSize, f.FileExtension, f.BaseUrl, f.FileUrl, f.FilePath, f.CUser userCreated, f.CDate dateTimeCreated "
      + " FROM T_BoardFile f inner join T_Board b inner join T_BoardSettings s on (s.BoardCode = b.BoardCode) on (b.BoardSeq = f.BoardSeq) "
      + " where f.BoardSeq = #{boardItemId} and b.BoardCode = #{boardCode} and s.Status ='ACTIVE'")
  public List<BoardFile> findAllFiles(@Param("boardCode") final String boardCode,
      @Param("boardItemId") final long boardItemId);

  @Select("SELECT f.BoardFileSeq fileId, f.FileName, f.FileSize, f.FileExtension, f.BaseUrl, f.FileUrl, f.FilePath, f.CUser userCreated, f.CDate dateTimeCreated "
      + " FROM T_BoardFile f inner join T_Board b inner join T_BoardSettings s on (s.BoardCode = b.BoardCode) on (b.BoardSeq = f.BoardSeq) "
      + " where f.BoardFileSeq = #{fileId} and b.BoardCode = #{boardCode} and s.Status ='ACTIVE'")
  public BoardFile findOneFile(@Param("boardCode") final String boardCode, @Param("fileId") final long fileId);

  // Board Comment
  /**
   * 게시물의 답변 (하위 모든 답변 포함) 목록을 조회한다.
   * 
   * @param boardCode   게시판 코드
   * @param boardItemId 게시물 ID
   * @return
   */
  /* PCommentSeq = 0 쿼리에서 제외 부모의 댓글을 지웠을경우 안보인다.
   * 가상테이블 cte 쿼리조회시 distinct 추가 
  */
  @Select("with recursive cte as (select BoardSeq boardItemId, BoardCommentSeq boardCommentId, PCommentSeq parentCommentId, Contents,UserId, WriterId, WriterName, UDate dateUpdated , CUser userCreated from T_BoardComment "
      + " where BoardSeq = #{boardItemId}"
      + " union all select p.BoardSeq boardItemId, p.BoardCommentSeq boardCommentId, p.PCommentSeq parentCommentId, p.Contents,p.UserId, p.WriterId, p.WriterName, UDate dateUpdated, CUser userCreated"
      + " from T_BoardComment p inner join cte on (p.PCommentSeq = cte.boardCommentId)) " +
      " select distinct cte.* from cte inner join T_Board b inner join T_BoardSettings s on (s.BoardCode = #{boardCode} and s.BoardCode = b.BoardCode and s.Status = 'ACTIVE') on (b.BoardSeq = cte.boardItemId) "
      +
      " order by cte.boardItemId desc")
  public List<BoardComment> findBoardComments(@Param("boardCode") final String boardCode,
      @Param("boardItemId") final long boardItemId);

  /**
   * 게시물의 답변에 등록된 모든 하위 답변 목록을 조회한다.
   * 
   * @param boardCode 게시판 코드
   * @param commentId 답변 ID
   * @return
   */
  @Select("with recursive cte as (select BoardSeq boardItemId, BoardCommentSeq boardCommentId, PCommentSeq parentCommentId, Contents,UserId, WriterId, WriterName, 1 as level, CAST(BoardCommentSeq AS CHAR(500)) AS path, UDate dateUpdated from T_BoardComment "
      + " where BoardCommentSeq = #{commentId} "
      + " union all select p.BoardSeq boardItemId, p.BoardCommentSeq boardCommentId, p.PCommentSeq parentCommentId, p.Contents,p.UserId, p.WriterId, p.WriterName, cte.level + 1, CONCAT(cte.path, ',', p.BoardCommentSeq), UDate dateUpdated "
      + " from T_BoardComment p inner join cte on (p.PCommentSeq = cte.boardCommentId)) " +
      " select cte.* from cte inner join T_Board b inner join T_BoardSettings s on (s.BoardCode = #{boardCode} and s.BoardCode = b.BoardCode and s.Status = 'ACTIVE') on (b.BoardSeq = cte.boardItemId) "
      +
      " order by path")
  public List<BoardComment> findChildBoardComments(@Param("boardCode") final String boardCode,
      @Param("commentId") final long commentId);

  /**
   * 게시물의 답변과 해당 답변의 하위 모든 답변을 삭제한다.
   * 
   * @param commentId 답변 ID
   * @return
   */

  /* 상위 부모댓글을 삭제하면 하위 답변이 다삭제되므로 일부 query 수정   where cte.BoardCommentSeq = #{commentId}*/
  @Delete("delete from T_BoardComment where BoardCommentSeq in (with recursive cte as (select BoardCommentSeq from T_BoardComment "
      + " where BoardCommentSeq = #{commentId} union all select p.BoardCommentSeq from T_BoardComment p inner join cte on (p.PCommentSeq = cte.BoardCommentSeq)) "
      + " select cte.* from cte where cte.BoardCommentSeq = #{commentId} )")
  public int deleteChildBoardComments(@Param("commentId") final long commentId);

  /**
   * 게시물에 등록된 모든 답변들을 삭제한다.
   * 
   * @param boardItemId 게시물 ID
   * @return
   */
  @Delete("delete from T_BoardComment where BoardSeq = #{boardItemId}")
  public int deleteAllBoardComments(@Param("boardItemId") final long boardItemId);

  /**
   * 게시물의 답변을 삭제한다.
   * 
   * @param boardCommentId
   * @return
   * @deprecated 게시물의 답변에 하위 답변이 존재할 수 있으므로, 1개의 답변만 삭제하는 기능은 권장하지 않음.
   *             {@link #deleteChildBoardComments(long) deleteChildBoardComments
   *             사용 권장.}
   */


  /**
   * 게시물의 답변을 조회한다.
   * 
   * @param boardCode   게시판 코드
   * @param boardItemId 게시물 ID
   * @return
   */
  @Select("SELECT c.BoardCommentSeq boardCommentId, c.BoardSeq boardItemId, c.Contents,c.UserId, c.WriterId, c.WriterName, c.PCommentSEq parentCommentId, c.CUser userCreated, c.CDate dateCreated, c.UUser userupdated, c.UDate dateUpdated FROM T_BoardComment c inner join T_Board b inner join T_BoardSettings s on (s.BoardCode = b.BoardCode) on (b.BoardSeq = c.BoardSeq) "
      + "WHERE c.BoardSeq = #{boardItemId} and s.BoardCode = #{boardCode} and s.Status = 'ACTIVE'")
  public List<BoardComment> findOnBoardComment(@Param("boardCode") final String boardCode,
      @Param("boardItemId") final long boardItemId);

  /**
   * 게시물의 답변을 등록한다.
   * 
   * @param item
   * @return
   */
  @Insert("INSERT INTO T_BoardComment (BoardSeq, Contents,UserId, WriterId, WriterName, PCommentSeq, CDate, CUser, UDate, UUser)"
      + "VALUES(#{boardItemId}, #{contents},#{userId}, #{writerId}, #{writerName}, #{parentCommentId}, #{dateCreated}, #{userCreated}, #{dateUpdated}, #{userUpdated})")
  public int addBoardComment(BoardComment item);

  /**
   * 게시물의 답변을 변경한다.
   * 
   * @param item
   * @return
   */
  @Update("UPDATE T_BoardComment SET Contents=#{contents}, WriterId=#{writerId}, WriterName=#{writerName}, UUser=#{userUpdated}, UDate=#{dateUpdated} WHERE BoardCommentSeq=#{boardCommentId}")
  public int updateBoardComment(BoardComment item);


  @Update("UPDATE T_Board SET NewBstate =#{newBstate}, NewMstate = #{newMstate} WHERE BoardSeq = #{boardItemId}")
  public int updateNewBoardState(Board board);

}
