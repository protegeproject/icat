package edu.stanford.bmir.protege.web.server;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.User;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultUser;

/**
 */
public class WatchedEntitiesCacheTest {
    @BeforeClass
	public void setUp(){
        WatchedEntitiesCache.purgeCache();
    }
    
    @Test
    public void testGetWatchedEntitiesByProjectAndUserWithOneResult(){
        final User user = new MockDefaultUser();
        user.setName("userName");
        WatchedEntitiesCache.addEntityWatch("myProject", "watch", user);
        assertTrue(WatchedEntitiesCache.isWatchedEntity("myProject", "userName", "watch"));
    }

    @Test
    public void testIsWatchedEntitiesByProjectAndUserWithOneResult(){
        final User user = new MockDefaultUser();
        user.setName("userName");
        WatchedEntitiesCache.addEntityWatch("myProject", "watch", user);
        assertTrue(WatchedEntitiesCache.isWatchedEntity("myProject", "userName", "watch"));
        assertFalse(WatchedEntitiesCache.isWatchedEntity("myProject", "userName", "bleah"));
        assertFalse(WatchedEntitiesCache.isWatchedEntity("myProject", "dummyUser", "bleah"));
        assertFalse(WatchedEntitiesCache.isWatchedEntity("dummyProject", "dummyUser", "bleah"));

    }

    @Test
    public void testIsWatchedBranchesByProjectAndUserWithOneResult(){
        final User user = new MockDefaultUser();
        user.setName("userName");
        WatchedEntitiesCache.addBranchWatch("myProject", "watch", user);
        assertTrue(WatchedEntitiesCache.isWatchedBranch("myProject", "userName", "watch"));
        assertFalse(WatchedEntitiesCache.isWatchedBranch("myProject", "userName", "bleah"));
        assertFalse(WatchedEntitiesCache.isWatchedBranch("myProject", "dummyUser", "bleah"));
        assertFalse(WatchedEntitiesCache.isWatchedBranch("dummyProject", "dummyUser", "bleah"));

    }

    @Test
    public void testGetWatchedBranchesByProjectAndUserWithOneResult(){
        final User user = new MockDefaultUser();
        user.setName("userName");
        WatchedEntitiesCache.addBranchWatch("myProject", "watch", user);
        assertTrue(WatchedEntitiesCache.isWatchedBranch("myProject", "userName", "watch"));
    }


    private static class MockDefaultUser extends DefaultUser {
        String name;

        @Override
        public void setName(String newName) {
            this.name = newName;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
