/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.s3.vfs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.pentaho.amazon.s3.S3Util;

import java.util.Collection;

public class S3FileSystem extends AbstractFileSystem implements FileSystem {

  private String awsAccessKeyCache;
  private String awsSecretKeyCache;
  private AmazonS3 client;

  protected S3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  @SuppressWarnings( "unchecked" )
  protected void addCapabilities( Collection caps ) {
    caps.addAll( S3FileProvider.capabilities );
  }

  protected FileObject createFile( AbstractFileName name ) throws Exception {
    return new S3FileObject( name, this );
  }

  public AmazonS3 getS3Client() {
    if ( client == null || hasClientChangedCredentials() ) {
      try {
        client = AmazonS3ClientBuilder.standard()
          .enableForceGlobalBucketAccess()
          .withRegion( Regions.DEFAULT_REGION )
          .build();
        awsAccessKeyCache = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
        awsSecretKeyCache = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
      } catch ( Throwable t ) {
        System.out.println( "Could not get an S3Client" );
        t.printStackTrace();
      }
    }
    return client;
  }

  private boolean hasClientChangedCredentials() {
    return client != null
      && ( S3Util.hasChanged( awsAccessKeyCache, System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY ) )
      || S3Util.hasChanged( awsSecretKeyCache, System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY ) ) );
  }
}
