package com.ecoinsight.bdsp.asd.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

import com.ecoinsight.bdsp.asd.entity.BoardComment;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

public interface IBoardCommentRepository extends IEntityRepository {
    class BoardCommentSqlProvider implements ProviderMethodResolver{

    }

    @Insert("INSERT INTO T_BoardComment (BoardCommentSeq, BoardSeq, Contents, WriterId, RefGroupNo, Step, RefOrder, ParentCommentSeq, ChildCommentSeq, CUser, CDate, UUser, UDate,Status)"
   + "VALUES(#{boardCommentId},#{boardItemId},#{contents},#{writerId},#{refGroupNo},#{step},#{refOrder},#{parentCommentSeq},#{childCommentSeq},#{userCreated}, #{dateCreated}, #{userUpdated}, #{dateUpdated})")
   public void add(BoardComment boardComment);





   



}
